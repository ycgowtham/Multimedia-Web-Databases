import numpy


def get_similarity_mapping(user_movies_list, user_movies, rest_movies_list,
                           rest_movies):
    movie_similarity = {}

    for i in range(len(user_movies_list)):
        user_movie = user_movies_list[i]
        for j in range(len(rest_movies_list)):
            check_movie = rest_movies_list[j]
#            a = 0
#            b = 0
#            p = 0
#            for k in range(len(rest_movies[j])):
#                value_1 = user_movies[i][k]
#                value_2 = rest_movies[j][k]
#                a += value_1 ** 2
#                b += value_2 ** 2
#                p += value_1 * value_2
#            if a == 0 or b == 0:
#                similarity_measure = 0
            # cosine = p / (math.sqrt(a) * math.sqrt(b))
            similarity_measure = numpy.dot(user_movies[i], rest_movies[j])
            # movie_similarity[user_movie][check_movie] = similarity_measure
            if not user_movie in movie_similarity:
                movie_similarity[user_movie] = []

            movie_similarity[user_movie].append((check_movie, similarity_measure))

    return movie_similarity

'''
user_movies_list = ['m1', 'm2', 'm3']
user_movies = [[0,1,0,1.5,0.5],
               [0,0.2,1.4,1.6,0.8],
               [1.1,3.0,1.5,0.7,0.9]]
rest_movies_list = ['m4', 'm5', 'm6', 'm7', 'm8']
rest_movies = [[0.1, 0.2, 1.0, 0.5, 0.3],
               [0.4, 0.2, 1.0, 1.5, 1.3],
               [1.1, 0.5, 0.1, 0.2, 0.2],
               [1.5, 0.3, 0.3, 0.1, 0.5],
               [0.1, 0.2, 1.2, 0.7, 1.2]]
result = get_similarity_mapping(user_movies_list, user_movies,
                                rest_movies_list, rest_movies)
print(result)
'''
