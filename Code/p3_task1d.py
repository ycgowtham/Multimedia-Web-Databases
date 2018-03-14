
from matrix_builder import build_movie_movie_matrix
from task3a import random_walk_restarts_on_matrix
from recommendation_tools import weigh_similarities



def recommender_system_using_ppr(user_id,
                                 users_watched_movies,
                                 movies_tfidf_tags_vector):

    """
    Recommend a set of movies based on the user's watching history.
    :param user_id: user id indicates which user.
    :param users_watched_movies: the movies watched by each user.
    :param movies_tfidf_tags_vector: tf-idf tag vector of all movies.
    :return: a dictionary contains the recommendation movis list for each movie watched by the user.
    { 'movie_id' : [(recommend 1, rank), (recommend 2, rank)], 'movie_id' : [(recommend 1, rank), (recommend 2, rank)] }
    This result need a weighted fuzzy merge to get final result.
    """
    if user_id not in users_watched_movies:
        print("user {} has empty watch history, no recommendation".format(user_id))
        return {}

    recommend_list = {}
    movies = movies_tfidf_tags_vector
    for movie in users_watched_movies[user_id]:
        if movie not in movies:
            movies[movie] = {}

    movie_matrix, movies, movies_index = build_movie_movie_matrix(movies)

    for movie in users_watched_movies[user_id]:
        recommend_list[movie] = random_walk_restarts_on_matrix(movie_matrix, movies, movies_index, [movie])

    recommend_list = weigh_similarities(user_id, recommend_list)

    return recommend_list
