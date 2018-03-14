import numpy as np
from matrix_builder import build_movie_movie_matrix

from task3a import random_walk_restarts_on_matrix


def get_movies_set_from_actor(actor, user_watched_movies, watched_movies_info, movies_tags_vector):
    movies = {}
    for movie in watched_movies_info:
        for a in actor:
            if a in watched_movies_info[movie]['actors']:
                movies[movie] = movies_tags_vector[movie]

    # print(movies)

    for movie in user_watched_movies:
        movies[movie] = movies_tags_vector[movie]

    return movies


def get_movies_set_from_genre(genre, user_watched_movies, genres_movie_list, movies_tags_vetor):
    movies = {}
    for movie in genres_movie_list[genre]:
        movies[movie] = movies_tags_vetor[movie]

    for movie in user_watched_movies:
        movies[movie] = movies_tags_vetor[movie]

    return movies


def get_movies_set_from_year(year, user_watched_movies, watched_movies_info, movies_tags_vector):
    movies = {}
    for movie in watched_movies_info:
        if watched_movies_info[movie]['year'] == year:
            movies[movie] = movies_tags_vector[movie]

    for movie in user_watched_movies:
        movies[movie] = movies_tags_vector[movie]

    return movies


def get_movies_set_from_rating(rating, user_watched_movies, watched_movies_info, movies_tags_vector):
    movies = {}
    for movie in watched_movies_info:
        if len(watched_movies_info[movie]['ratings']) > 0 and \
                np.mean(watched_movies_info[movie]['ratings']) >= rating:
            movies[movie] = movies_tags_vector[movie]

    for movie in user_watched_movies:
        movies[movie] = movies_tags_vector[movie]

    return movies


def get_movies_set_from_all(user_watched_movies, watched_movies_info, movies_tags_vector):
    movies = {}

    for movie in watched_movies_info:
        movies[movie] = movies_tags_vector[movie]

    for movie in user_watched_movies:
        movies[movie] = movies_tags_vector[movie]

    return movies


def get_user_preferred_actor(user_watched_movies, watched_movies_info):
    actors = {}

    for movie in user_watched_movies:
        actor = watched_movies_info[movie]['actors']
        for a in actor:
            if a not in actors:
                actors[a] = 0
            actors[a] += 1

    return actors


def get_user_preferred_genre(user_watched_movies, watched_movies_info):
    genres = {}

    for movie in user_watched_movies:
        genre = watched_movies_info[movie]['genres']
        for g in genre:
            if g not in genres:
                genres[g] = 0
            genres[g] += 1

    # print(genres)

    return sorted(genres.items(), key=lambda x: x[1], reverse=True)[0][0]


def get_user_preferred_year(user_watched_movies, watched_movies_info):
    years = {}

    for movie in user_watched_movies:
        year = watched_movies_info[movie]['year']
        if year not in years:
            years[year] = 0
        years[year] += 1

    # print(years)

    return sorted(years.items(), key=lambda x: x[1], reverse=True)[0][0]


def get_user_preferred_rating(user_watched_movies, watched_movies_info):
    ratings = []

    for movie in user_watched_movies:
        if len(watched_movies_info[movie]['ratings']) > 0:
            ratings.append(np.mean(watched_movies_info[movie]['ratings']))

    return np.mean(ratings)


def recommend_using_ppr(movies, watched_list):
    movie_matrix, movies, movies_index = build_movie_movie_matrix(movies)
    return random_walk_restarts_on_matrix(movie_matrix, movies, movies_index, watched_list)


def rank_joint(results, user_watched_movies):
    movies = {}
    for result in results:
        for r in result:
            if r[0] in user_watched_movies:
                continue
            if r[0] not in movies:
                movies[r[0]] = 0
            movies[r[0]] += r[1]

    return sorted(movies.items(), key=lambda x: x[1], reverse=True)[:10]


def recommender_system(user_id, users_watched_movies, watched_movies_info,
                       genres_movie_list, movies_tfidf_tags_vector):

    if user_id not in users_watched_movies:
        print('User :', user_id, ' empty watch list, no recommendation')
        return []

    results = []
    # analysis on watched movies (genre actor rating year).

    # preferred actor -> 10 related actors(task1c) -> movies played by these actors
    actors = get_user_preferred_actor(users_watched_movies[user_id], watched_movies_info)
    # print('user :', user_id, ' preferred actors: ', actors)
    # movie-movie similarity matrix RWR -> 10 movies
    movies = get_movies_set_from_actor(actors, users_watched_movies[user_id], watched_movies_info, movies_tfidf_tags_vector)
    results.append(recommend_using_ppr(movies, users_watched_movies[user_id]))

    # preferred genre -> movies from this genre
    genre = get_user_preferred_genre(users_watched_movies[user_id], watched_movies_info)
    # print('user :', user_id, ' preferred genre: ', genre)
    # movie-movie similarity matrix RWR -> 10 movies
    movies = get_movies_set_from_genre(genre, users_watched_movies[user_id], genres_movie_list, movies_tfidf_tags_vector)
    results.append(recommend_using_ppr(movies, users_watched_movies[user_id]))

    # preferred year -> movies from this year
    year = get_user_preferred_year(users_watched_movies[user_id], watched_movies_info)
    # print('user :', user_id, ' preferred year: ', year)
    # movie-movie similarity matrix RWR -> 10 movies
    movies = get_movies_set_from_year(year, users_watched_movies[user_id], watched_movies_info, movies_tfidf_tags_vector)
    results.append(recommend_using_ppr(movies, users_watched_movies[user_id]))

    # preferred rating -> movies above this rating.
    rating = get_user_preferred_rating(users_watched_movies[user_id], watched_movies_info)
    # print('user :', user_id, ' preferred rating: ', rating)
    # movie-movie similarity matrix RWR -> 10 movies
    movies = get_movies_set_from_rating(rating, users_watched_movies[user_id], watched_movies_info, movies_tfidf_tags_vector)
    results.append(recommend_using_ppr(movies, users_watched_movies[user_id]))

    # no preference -> all movies.
    movies = get_movies_set_from_all(users_watched_movies[user_id], watched_movies_info, movies_tfidf_tags_vector)
    results.append(recommend_using_ppr(movies, users_watched_movies[user_id]))

    # Rank joint (Fuzzing merge) (5 * 10 movies to one)
    return rank_joint(results, users_watched_movies[user_id])
