from tensorly import decomposition
from tensorly import tensor
from p3_task1a import split_output
from similarity_metrics import get_similarity_mapping
from recommendation_tools import weigh_similarities

def recommender_system_using_cp(user_id, user_movies, movie_tag_vector,
                                tag_movie_rating, tmr_tensor_info,
                                genome_tags):

    movies_watched = list(user_movies[user_id])

    movies_watched_tags = {}
    for movie in movies_watched:
        movie_tags = movie_tag_vector[movie]
        for tag in list(movie_tags.keys()):
            movies_watched_tags[tag] = 1
    tags = list(movies_watched_tags.keys())

    # tags = list(genome_tags.keys())

    all_movies_indexed = sorted(tmr_tensor_info[3].items(), key=lambda x: x[1])
    all_movies = [a for a,b in all_movies_indexed]


    U = tag_movie_rating
    T = tensor(U.reshape((U.shape[0], U.shape[1], U.shape[2])))
    P = decomposition.parafac(T, 5, init="random")

    X = P[0].asnumpy();
    Y = P[1].asnumpy();
    Z = P[2].asnumpy();

    watched_indexed, U_watched, rest_indexed, U_rest \
        = split_output(Y, movies_watched, all_movies)

    similarity_mapping = get_similarity_mapping(watched_indexed,
                                                U_watched,
                                                rest_indexed,
                                                U_rest)

    weighted_similarities = weigh_similarities(user_id,
                                               similarity_mapping)

    return weighted_similarities
