import numpy as np
from sklearn.decomposition import PCA
import gensim

import utils as lib


def generate_corpus(tag_dict):
    corpus = []
    document = []
    corpus.append(document)

    for tag, count in tag_dict.items():
        document.append((int(tag), count))

    return corpus


def generate_model(tag_dict):
    corpus = generate_corpus(tag_dict)

    lda = gensim.models.LdaModel(corpus, num_topics=5)

    return lda, lda[corpus]


def get_top_10_using_LDA_model(actor_list, model, topic_terms, actor_tags):
    topic_similarities = {}
    for actor in actor_tags:
        if actor not in actor_list:
            topic_similarities[actor] = model[generate_corpus(actor_tags[actor])]

    chosen_actor_topics = convert_to_vector(topic_terms[0])
    dot_products = {}

    for actor in topic_similarities:
        v2 = convert_to_vector(topic_similarities[actor][0])
        if len(v2) == 5:
            dot_products[actor] = np.dot(v2, chosen_actor_topics)

    return dot_products


def convert_to_vector(topics):
    v = []
    for topic in topics:
        v.append(topic[1])
    return v


def dict_to_matrix(movie_tags, actors_tags):
    """
    Convert a dictionary into matrix array, mapping movie and actors into same tag vector space
    :param movie_tags: movie object described by tag vector
    :param actors_tags: actors objects described by tag vector
    :return: matrix, matrix using tags as base vector movie + actors as objects,
             row_idx_name, row name -> row index relation
             col_idx_name, column name -> column index relation
    """
    tag_set = set(movie_tags.keys())
    actors = set()

    for actor in actors_tags:
        if len(actors_tags[actor]) == 0:
            continue
        tag_set = tag_set.union(actors_tags[actor].keys())
        actors.add(actor)

    rows = len(actors) + 1
    columns = len(tag_set)
    # key_set.union(actors_tags[actor].keys() for actor in actors_tags)
    matrix = np.zeros((rows, columns))
    tag_set = sorted(tag_set)
    for i, tag in enumerate(tag_set):
        if tag in movie_tags:
            matrix[0][i] = movie_tags[tag]

    actors = sorted(actors)
    for i, actor in enumerate(actors):
        for j, tag in enumerate(tag_set):
            actor_tags = actors_tags[actor]
            if tag in actor_tags:
                matrix[i + 1][j] = actor_tags[tag]

    return matrix, actors, tag_set


def find_related_actors_of_a_movie(movie_id, movie_actor_list, movie_tfidf_tags, movie_tags, actor_tfidf_tags, actor_tags, mode):
    """
    Task 1d, given a movie, finding the top10 most related actors who have not acted in the movie, leveraging the given
    movie's TF-IDF tag vectors, top 5 latent semantics in the space of tags.
    :param movie_id: Movie id given by user
    :param movie_actor_list: A list contains the actors acted in this movie
    :param movie_tfidf_tags: Movie mapped into tf-idf tags vector
    :param movie_tags: Movie mapped into raw tags vector
    :param actor_tfidf_tags: Actors mapped into tf-idf tags vector
    :param actor_tags: Actors mapped into raw tags vector
    :return:
    """
    if movie_id not in movie_tags:
        print('invalid movieid')
        return

    if mode == 'TF-IDF':

        result = {}
        for actor in actor_tfidf_tags:
            if actor in movie_actor_list[movie_id]:
                continue

            dot_product, cosine = lib.dot_product(actor_tfidf_tags[actor], movie_tfidf_tags[movie_id])
            # print(math.acos(cosine)/math.pi * 180)
            result[actor] = dot_product

        for r in sorted(result.items(), key=lambda x: x[1], reverse=True)[:10]:
            print('actor_id: ', r[0], 'dot_product: ', r[1])
        # sort cosine output top-10
    elif mode == 'SVD':
        # construct a matrix from movie_id, actors tags
        a, row_name, column_name = dict_to_matrix(movie_tfidf_tags[movie_id], actor_tfidf_tags)

        # SVD
        u, s, v = np.linalg.svd(a, full_matrices=False)

        result = {}
        # select top-5 latent semantics indies
        # print(np.allclose(a[0], np.dot(u[0], np.dot(np.diag(s), v))))

        for i, actor in enumerate(row_name):
            if actor in movie_actor_list[movie_id]:
                continue
            result[actor] = np.dot(u[0][:5], u[i + 1][:5])

        for r in sorted(result.items(), key=lambda x: x[1], reverse=True)[:10]:
            print('actor_id: ', r[0], 'dot_product: ', r[1])
    elif mode == 'PCA':
        # construct a matrix from movie_id, actors tags
        a, row_name, column_name = dict_to_matrix(movie_tfidf_tags[movie_id], actor_tfidf_tags)

        # PCA
        pca = PCA()
        u = pca.fit_transform(a)

        result = {}
        # select top-5 latent semantics indies

        for i, actor in enumerate(row_name):
            if actor in movie_actor_list[movie_id]:
                continue
            result[actor] = np.dot(u[0][:5], u[i + 1][:5])

        for r in sorted(result.items(), key=lambda x: x[1], reverse=True)[:10]:
            print('actor_id: ', r[0], 'dot_product: ', r[1])
    elif mode == 'LDA':
        movie = movie_tags[movie_id]
        for actor in actor_tags:
            for tag in actor_tags[actor]:
                if tag not in movie:
                    movie[tag] = 0

        model, topic_terms = generate_model(movie)
        latent_semantics = get_top_10_using_LDA_model(movie_actor_list[movie_id], model, topic_terms, actor_tags)
        for actor in sorted(latent_semantics.items(), key=lambda x: x[1], reverse=True)[:10]:
            print('actor_id: ', actor[0], ' dot_product: ', actor[1])
    else:
        print('unsupported mode')
