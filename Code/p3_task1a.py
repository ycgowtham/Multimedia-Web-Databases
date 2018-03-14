import numpy as np
from matrix_builder import build_movie_tag_matrix
from sklearn.decomposition.pca import PCA
from similarity_metrics import get_similarity_mapping
from recommendation_tools import weigh_similarities

def recommender_system_using_svd_pca(user_id, user_movies, movie_tag_vector,
                                     genome_tags, model):
    movies_watched = list(user_movies[user_id])

    movies_watched_tags = {}
    for movie in movies_watched:
        movie_tags = movie_tag_vector[movie]
        for tag in list(movie_tags.keys()):
            movies_watched_tags[tag] = 1
    tags = list(movies_watched_tags.keys())

    # tags = list(genome_tags.keys())
    all_movies = list(movie_tag_vector.keys())
    all_movies, movie_tag_matrix = build_movie_tag_matrix(all_movies, tags,
                                               movie_tag_vector)

    if model == 'PCA':
        pca = PCA(n_components=min(10, len(tags)))
        U = pca.fit_transform(movie_tag_matrix)
    else:
        U, S, Vt = np.linalg.svd(movie_tag_matrix, full_matrices=False)

    watched_indexed, U_watched, rest_indexed, U_rest \
        = split_output(U, movies_watched, all_movies)

    similarity_mapping = get_similarity_mapping(watched_indexed,
                                                U_watched,
                                                rest_indexed,
                                                U_rest)

    weighted_similarities = weigh_similarities(user_id,
                                               similarity_mapping)

    return weighted_similarities

def split_output(U, movies_watched, all_movies):
    U_watched = []
    U_rest = []
    watched_indexed = []
    rest_indexed = []
    for i in range(len(all_movies)):
        if all_movies[i] in movies_watched:
            watched_indexed.append(all_movies[i])
            U_watched.append(U[i])
        else:
            rest_indexed.append(all_movies[i])
            U_rest.append(U[i])
    return watched_indexed, U_watched, rest_indexed, U_rest
