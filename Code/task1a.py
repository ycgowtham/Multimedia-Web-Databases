import numpy as np
from sklearn.decomposition import PCA
import gensim

def find_genre_latent_topics_from_tags(data_vector, tag_info, model, top_n):
    tag_index = get_indexed_tags(tag_info)

    if model == 'PCA':
        data = get_data_matrix(data_vector, list(tag_info), float)
        top_n_vectors = get_PCA_output(data, top_n)
        for i in range(top_n):
            dict = {}
            print("Latent Semantic ", i)
            for j in range(len(top_n_vectors[i])):
                dict[tag_index[j]] = top_n_vectors[i][j]
            result = sorted(dict.items(), key=lambda x: x[1], reverse=True)
            print_tag_vector_result(result, tag_info)

    elif model == 'SVD':
        data = get_data_matrix(data_vector, list(tag_info), float)
        top_n_vectors = get_SVD_output(data, top_n)
        for i in range(top_n):
            dict = {}
            print("Latent Semantic ", i+1)
            for j in range(len(top_n_vectors[i])):
                dict[tag_index[j]] = top_n_vectors[i][j]
            result = sorted(dict.items(), key=lambda x: x[1], reverse=True)
            print_tag_vector_result(result, tag_info)

    elif model == 'LDA':
        data = get_data_matrix(data_vector, list(tag_info), int)
        top_n_vectors = get_LDA_output_for_tags(data, tag_info, top_n)
        pass


def find_genre_latent_topics_from_actors(data_vector, actor_info, model, top_n):
    if model == 'PCA':
        data = get_data_matrix(data_vector, actor_info, float)
        top_n_vectors = get_PCA_output(data, top_n)
        for i in range(top_n):
            dict = {}
            print("Latent Semantic", i+1)
            for j in range(len(top_n_vectors[i])):
                dict[actor_info[j]] = top_n_vectors[i][j]
            result = sorted(dict.items(), key=lambda x: x[1], reverse=True)
            print_actor_vector_result(result, actor_info)

    elif model == 'SVD':
        data = get_data_matrix(data_vector, actor_info, float)
        top_n_vectors = get_SVD_output(data, top_n)
        for i in range(top_n):
            dict = {}
            print("Latent Semantic", i+1)
            for j in range(len(top_n_vectors[i])):
                dict[actor_info[j]] = top_n_vectors[i][j]
            result = sorted(dict.items(), key=lambda x: x[1], reverse=True)
            print_actor_vector_result(result, actor_info)

    elif model == 'LDA':
        data = get_data_matrix(data_vector, actor_info, int)
        top_n_vectors = get_LDA_output_for_actors(data, actor_info, top_n)
        pass


def print_tag_vector_result(result, tag_info):
    c = 0
    for r in result:
        print(tag_info[r[0]], r[1])
        c += 1
        if c == 5:
            # break
            pass
    print()


def print_actor_vector_result(result, actor_info):
    c = 0
    for r in result:
        print("actor ID=" + r[0],"\t : ", r[1])
        c += 1
        if c == 5:
            # break
            pass
    print()


def get_PCA_output(data, top_n):
    pca = PCA(n_components=5)
    pca.fit(data)
    return pca.components_[:top_n]


def get_SVD_output(data, top_n):
    U, S, Vt = np.linalg.svd(data, full_matrices=False)
    return Vt[:top_n]


def get_LDA_output_for_tags(data, tag_info, top_n):
    tags = list(tag_info)

    #method 1 - Using LDA from lda (resultant topic_dist is between 0,1)
    # lda_model = lda.LDA(n_topics=top_n, n_iter=10, random_state=1) # do not change
    # lda_model.fit(data) # do not change it
    # topic_word = lda_model.topic_word_
    # n_top_words = 10
    # for i, topic_dist in enumerate(topic_word):
    #     print(topic_dist)
    #     topic_words = np.array(tags)[np.argsort(topic_dist)][:-n_top_words:-1]
    #     topic_labels = []
    #     for tag in topic_words:
    #         topic_labels.append(tag_info[tag])
    #     print('Topic {}: {}'.format(i, ', '.join(topic_labels)))

    #method 2 - Using LatentDirichletAllocation from sklearn
    # lda_actor = LatentDirichletAllocation(n_components=top_n, max_iter=10,
    #                                       learning_method='online',
    #                                       learning_offset=50., random_state=0)
    # lda_actor.fit_transform(data)
    # for i, topic_dist in enumerate(lda_actor.components_):
    #     # print(topic_dist)
    #     topic_words = np.array(tags)[np.argsort(topic_dist)][:-10:-1]
    #     topic_labels = []
    #     for tag in topic_words:
    #         topic_labels.append(tag_info[tag])
    #     print('Topic {}: {}'.format(i, ', '.join(topic_labels)))


    # method 3 - Using gensim
    # D = []
    # for i in range(len(data)):
    #     D.append([])
    #     for j in range(len(tags)):
    #         D[i].append((j,data[i][j]))
    # lda = gensim.models.LdaModel(corpus=D, num_topics=top_n)
    # for i in range(top_n):
    #     print("Topic", i+1)
    #     result = lda.top_topics(corpus=D, num_words=None)[i][0]
    #     for j in range(len(result)):
    #         print(tag_info[tags[int(result[j][1])]], " : ", result[j][0])
    #     print()
    #
    #
    # method 3 - using gensim new
    # method 3 - Using gensim
    tags = list(tag_info)
    D = []
    for i in range(len(data)):
        D.append([])
        for j in range(len(tags)):
            D[i].append((j, data[i][j]))
    temp = {}
    for i, tag in enumerate(tags):
        temp[i] = tag
    lda = gensim.models.LdaModel(corpus=D, num_topics=top_n)
    result = lda.print_topics(top_n)
    output = {}
    for i in range(top_n):
        output[i] = {}
        val = result[i][1]
        arr = val.split(" + ")
        for j in range(len(arr)):
            items = arr[j].split("*")
            tagid = items[1].split("\"")[1]
            output[i][tag_info[tags[int(tagid)]]] = items[0]
        print("Topic", i + 1, " : ", output[i])


def get_LDA_output_for_actors(data, actors, top_n):
    #method 1 - Using LDA from lda (resultant topic_dist is between 0,1)
    # lda_model = lda.LDA(n_topics=top_n, n_iter=10, random_state=1) # do not change
    # lda_model.fit(data) # do not change it
    # topic_word = lda_model.topic_word_
    # n_top_words = 10
    # for i, topic_dist in enumerate(topic_word):
    #     print(topic_dist)
    #     topic_words = np.array(actors)[np.argsort(topic_dist)][:-n_top_words:-1]
    #     print('Topic {}: {}'.format(i, ' '.join(topic_words)))


    #method 2 - Using LatentDirichletAllocation from sklearn
    # lda_actor = LatentDirichletAllocation(n_components=top_n, max_iter=10,
    #                                       learning_method='online',
    #                                       learning_offset=50., random_state=0)
    # lda_actor.fit_transform(data)
    # for i, topic_dist in enumerate(lda_actor.components_):
    #     print(topic_dist)
    #     topic_words = np.array(actors)[np.argsort(topic_dist)][:-10:-1]
    #     print('Topic {}: {}'.format(i, ', '.join(topic_words)))

    # method 3 - Using gensim
    # D = []
    # for i in range(len(data)):
    #     D.append([])
    #     for j in range(len(actors)):
    #         D[i].append((j,data[i][j]))
    # lda = gensim.models.LdaModel(D, num_topics=top_n)
    # for i in range(top_n):
    #     print("Topic", i+1)
    #     result = lda.top_topics(corpus=D, num_words=None)[i][0]
    #     for j in range(len(result)):
    #         print("actor_id =",actors[int(result[j][1])], " : ", result[j][0])
    #     print()

    # method 3 using gensim new
    # method 3 - Using gensim
    D = []
    for i in range(len(data)):
        D.append([])
        for j in range(len(actors)):
            D[i].append((j, data[i][j]))
    temp = {}
    for i, tag in enumerate(actors):
        temp[i] = tag
    lda = gensim.models.LdaModel(corpus=D, num_topics=top_n)
    result = lda.print_topics(top_n)
    output = {}
    for i in range(top_n):
        output[i] = {}
        val = result[i][1]
        arr = val.split(" + ")
        for j in range(len(arr)):
            items = arr[j].split("*")
            tagid = items[1].split("\"")[1]
            output[i][actors[int(tagid)]] = items[0]
        print("Topic", i + 1, " : ", output[i])

def print_top_words(model, feature_names, n_top_words):
    for topic_idx, topic in enumerate(model.components_):
        message = "Topic #%d: " % topic_idx
        message += " ".join([feature_names[i]
                             for i in topic.argsort()[:-n_top_words:-1]])
        print(message)
    print()


def get_data_matrix(data_vector, columns, type):
    D = np.zeros(shape=(len(data_vector), len(columns)), dtype=type)
    rows = list(data_vector.keys())
    for i in range(len(data_vector)):
        for tag in data_vector[rows[i]]:
            D[i][columns.index(tag)] = data_vector[rows[i]][tag]
    return D

'''
def get_covariance_matrix(data, genre, TAGS, GENRES):
    avg = get_average_genre_tag(data, TAGS)
    COV = np.zeros(shape=(len(TAGS), len(TAGS)))
    for i in range(len(TAGS)):
        tagi = data[GENRES.index(genre)][i]
        for j in range(len(TAGS)):
            tagj = data[GENRES.index(genre)][j]
            COV[i][j] = (tagi - avg[i]) * (tagj - avg[j])
    return COV


def get_average_genre_tag(data, TAGS):
    avg_genre_tag = []
    genre_count = len(data)
    for i in range(len(TAGS)):
        avg_genre_tag.append(0)
        for j in range(genre_count):
            avg_genre_tag[i] += data[j][i]
        avg_genre_tag[i] = avg_genre_tag[i]/genre_count
    return avg_genre_tag

'''


def get_indexed_tags(genome_tags):
    TAGS = []
    for tagid in genome_tags.keys():
        TAGS.append(tagid)
    return TAGS
