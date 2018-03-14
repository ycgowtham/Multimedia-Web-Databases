import numpy as np
import math
from similarity_metrics import get_similarity_mapping
from matrix_builder import build_movie_tag_matrix

header = []

def recommender_system_for_labeling_movies(movie_info,
                                           labelled_movies,
                                           genome_tags,
                                           movie_tag_vector,
                                           model, r):
    all_movies = [movie_info[i]['movieid'] for i in range(len(movie_info))]
    tags = list(genome_tags.keys())
    all_movies, movie_tag_matrix = build_movie_tag_matrix(all_movies, tags,
                                                          movie_tag_vector)
    matrix = get_dimension_reduced_matrix(movie_tag_matrix)
    # test_similarity_measure(movie_labels, all_movies, matrix)
    movie_labels = {}
    for label in labelled_movies:
        for movie in labelled_movies[label]:
            movie_labels[movie] = label

    if model == 'NN':
        new_labels = run_nearest_neighbor_classifier(matrix,
                                                     movie_labels,
                                                     labelled_movies,
                                                     all_movies, r)
        # for movie in new_labels:
        #     print("{0}: {1} ==> {2}".format(
        #         movie,
        #         movie_info[all_movies.index(movie)]['moviename'],
        #         new_labels[movie]))

    elif model == 'DT':
        new_labels = run_decision_tree_classifier(matrix,
                                                  labelled_movies,
                                                  all_movies)
    elif model == 'SVM':
        new_labels = run_nary_SVM_classifier(matrix,
                                             movie_labels,
                                             labelled_movies,
                                             all_movies)

    for movie in new_labels:
        print("{0}: {1} ==> {2}".format(
            movie,
            movie_info[all_movies.index(movie)]['moviename'],
            new_labels[movie]))
        pass
    pass


def run_nearest_neighbor_classifier(matrix,
                                    movie_labels,
                                    labelled_movies,
                                    all_movies, r):
    new_movie_labels = {}
    for movie in all_movies:
        if movie in movie_labels:
            continue
        vector1 = matrix[all_movies.index(movie)]
        movie_distance = {}
        for movie2 in movie_labels:
            vector2 = matrix[all_movies.index(movie2)]
            distance = 0
            for i in range(len(vector1)):
                distance += (vector2[i] - vector1[i]) ** 2
            distance = math.sqrt(distance)
            movie_distance[movie2] = distance
        sorted_list = sorted(movie_distance.items(), key=lambda x:x[1])

        labels = list(labelled_movies.keys())
        label_count = [0 for _ in labels]
        for i in range(r):
            m, val = sorted_list[i]
            index = labels.index(movie_labels[m])
            label_count[index] += 1
        max_count = max(label_count)
        index = label_count.index(max_count)
        new_movie_labels[movie] = labels[index]
    return new_movie_labels
    pass

def unique_vals(rows, col):
    return set([row[col] for row in rows])

def get_counts(rows):
    counts = {}
    for row in rows:
        label = row[-1]
        if label not in counts:
            counts[label] = 0
        counts[label] += 1
    return counts

def run_decision_tree_classifier(matrix, labelled_movies, all_movies):
    training_datasets=[]
    ind_list = []
    for key, value in labelled_movies.items():
        for i in value:
            ind = all_movies.index(int(i))
            x = matrix[ind]
            ind_list.append(x)
            new_list = x + [key]
            training_datasets.append(new_list)
    header = []
    for i in range(0, len(matrix[0])):
        header.append('F'+str(i))
    header.append("Label")

    my_tree = build_tree_based_on_conditions(training_datasets)
    i=0
    d_dictionary = {}
    for row in matrix:
        d_dictionary[all_movies[i]] = get_label_for_data(row, my_tree)
        i = i + 1
    return d_dictionary

def is_numeric(value):
    return isinstance(value, int) or isinstance(value, float)

def run_nary_SVM_classifier(matrix, movie_labels, labelled_movies, all_movies):
    SVM_values = {}
    for label in labelled_movies:
        SVM_values[label] = {}
        movie_list = labelled_movies[label]
        rest_movie_list = []
        for l in labelled_movies:
            if l == label:
                continue
            else:
                for m in labelled_movies[l]:
                    rest_movie_list.append(m)
        distance_metrics = get_distance_metrics(matrix, movie_list,
                                          rest_movie_list, all_movies)
        m1, m2 = get_min_distant_movies(distance_metrics)
        vector1 = matrix[all_movies.index(m1)]
        vector2 = matrix[all_movies.index(m2)]
        diff = [(vector2[i] - vector1[i]) for i in range(len(vector1))]
        mid_point = [(vector1[i] + vector2[i])/2 for i in range(len(vector1))]
        SVM_values[label]['diff'] = diff
        SVM_values[label]['midpoint'] = mid_point
    # print(SVM_values)

    labels = list(labelled_movies.keys())

    for label in labels:
        movie_list = labelled_movies[label]
        diff = SVM_values[label]['diff']
        mid_point = SVM_values[label]['midpoint']

        less_than = 0
        greater_than = 0
        val = 0
        for movie in movie_list:
            movie_vector = matrix[all_movies.index(movie)]
            for i in range(len(diff)):
                val += diff[i] * (movie_vector[i] - mid_point[i])
            if val < 0:
                less_than += 1
            else:
                greater_than += 1
        if less_than > greater_than:
            SVM_values[label]['compare'] = 0
        else:
            SVM_values[label]['compare'] = 1


    new_movie_labels = {}
    for movie in all_movies:
        label_count = [0 for _ in labels]
        if movie in movie_labels:
            continue
        movie_vector = matrix[all_movies.index(movie)]
        for label in labels:
            diff = SVM_values[label]['diff']
            mid_point = SVM_values[label]['midpoint']
            comparator = SVM_values[label]['compare']
            val = 0
            for i in range(len(diff)):
                val += diff[i] * (movie_vector[i] - mid_point[i])
            if val < 0 and comparator == 0:
                label_count[labels.index(label)] += 1
            elif val > 0 and comparator == 1:
                label_count[labels.index(label)] += 1
            else:
                label_count[labels.index(label)] -= 1
        max_count = max(label_count)
        index = label_count.index(max_count)
        new_movie_labels[movie] = labels[index]
    return new_movie_labels
    pass


def get_label_for_data(row, node):
    if isinstance(node, Leaf):
        return node.predictions
    if node.question.match(row):
        return get_label_for_data(row, node.positive_branch)
    else:
        return get_label_for_data(row, node.negative_branch)


def build_tree_based_on_conditions(rows):
    gain, question = get_conditions_to_divide(rows)
    if gain == 0:
        return Leaf(rows)
    true_rows, false_rows = partition(rows, question)
    positive_branch = build_tree_based_on_conditions(true_rows)
    negative_branch = build_tree_based_on_conditions(false_rows)
    return Conditional_Node(question, positive_branch, negative_branch)

class Conditional_Node:
    def __init__(self,
                 question,
                 positive_branch,
                 negative_branch):
        self.question = question
        self.positive_branch = positive_branch
        self.negative_branch = negative_branch

def test_similarity_measure(movie_labels, all_movies, matrix):
    for label in movie_labels:
        movie_list = list(movie_labels[label])
        labelled_movie_vector = []
        for movie in movie_list:
            index = all_movies.index(movie)
            labelled_movie_vector.append(matrix[index])
        similarity = get_similarity_mapping(movie_list, labelled_movie_vector,
                                            movie_list, labelled_movie_vector)
        for movie in movie_list:
            print('Movie similar to', movie)
            similar_movies = similarity[movie]
            for d in similar_movies:
                m, s = d
                print(m, s)
        print()
    pass

def partition(rows, question):
    true_rows, false_rows = [], []
    for row in rows:
        if question.match(row):
            true_rows.append(row)
        else:
            false_rows.append(row)
    return true_rows, false_rows

class Conditions:
    def __init__(self, column, value):
        self.column = column
        self.value = value

    def match(self, example):
        val = example[self.column]
        if is_numeric(val):
            return val >= self.value
        else:
            return val == self.value


def get_dimension_reduced_matrix(movie_tag_matrix):
    U, S, Vt = np.linalg.svd(movie_tag_matrix, full_matrices=False)
    dim = int(math.log2(len(U)))
    matrix = []
    for i in range(len(U)):
        matrix.append(list(U[i][:dim]))
    return matrix


def gini(rows):
    counts = get_counts(rows)
    impurity = 1
    for lbl in counts:
        prob_of_lbl = counts[lbl] / float(len(rows))
        impurity -= prob_of_lbl**2
    return impurity


def get_distance_metrics(matrix, movie_list, rest_movie_list, all_movies):
    distance_measure = {}
    for movie1 in movie_list:
        movie_distance = []
        vector1 = matrix[all_movies.index(movie1)]
        for movie2 in rest_movie_list:
            vector2 = matrix[all_movies.index(movie2)]
            distance = 0
            for i in range(len(vector1)):
                distance += (vector2[i] - vector1[i]) ** 2
            distance = math.sqrt(distance)
            movie_distance.append((movie2, distance))
        distance_measure[movie1] = movie_distance
    return distance_measure

def get_conditions_to_divide(rows):
    best_gain = 0
    best_question = None
    current_uncertainty = gini(rows)
    n_features = len(rows[0]) - 1

    for col in range(n_features):
        values = set([row[col] for row in rows])
        for val in values:
            question = Conditions(col, val)
            true_rows, false_rows = partition(rows, question)
            if len(true_rows) == 0 or len(false_rows) == 0:
                continue
            gain = info_gain(true_rows, false_rows, current_uncertainty)
            if gain >= best_gain:
                best_gain, best_question = gain, question
    return best_gain, best_question

def info_gain(left, right, current_uncertainty):
    p = float(len(left)) / (len(left) + len(right))
    return current_uncertainty - p * gini(left) - (1 - p) * gini(right)

def get_min_distant_movies(distance_measure):
    min_dist = 99999
    m1 = ''
    m2 = ''
    for movie_i in distance_measure:
        for movie_j, dist in distance_measure[movie_i]:
            if dist < min_dist:
                min_dist = dist
                m1 = movie_i
                m2 = movie_j
    return m1, m2


class Leaf:
    def __init__(self, rows):
        self.predictions = get_counts(rows)
        self.predictions = get_counts(rows)
