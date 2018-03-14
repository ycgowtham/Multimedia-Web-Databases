from numpy import zeros

from utils import dot_product

def build_actor_actor_matrix(actors_tag_vector):
    """
    Build actor similarity matrix based on the actors_tag_vector.
    :param actors_tag_vector: actors in tags vector space
    :return: actor_matrix, 2D matrix describes the actor similarity
             actors, sorted_actor_list, you can get (index in matrix)->(actor id)
             actors_index, actor_index_dict, you can get (actor id)->(index in matrix)
    """
    actors = sorted(actors_tag_vector.keys())
    actors_index = {}

    for i, v in enumerate(actors):
        actors_index[v] = i

    actor_matrix = zeros((len(actors), len(actors)))

    for i, _actor in enumerate(actors):
        for j, actor in enumerate(actors):
            actor_matrix[i][j] = dot_product(actors_tag_vector[_actor], actors_tag_vector[actor])[0]

    return actor_matrix, actors, actors_index


def build_coactor_coactor_matrix(movie_actor_table):
    """
    Build coactor matrix based on the movie_actor_table.
    :param movie_actor_table: movie_actor_table describes the actor-movie relationship.
    :return: coactor_matrix, 2D matrix describes the coactor relationships
             actors, sorted_actor_list, you can get (index in matrix)->(actor id)
             actors_index, actor_index_dict, you can get (actor id)->(index in matrix)
    """
    actors = set()
    actors_index = {}
    movies = {}

    for record in movie_actor_table:
        movies[record['movieid']] = set()

    for record in movie_actor_table:
        movies[record['movieid']].add(record['actorid'])
        actors.add(record['actorid'])

    coactor_matrix = zeros((len(actors), len(actors)))

    actors = sorted(actors)
    for i, v in enumerate(actors):
        actors_index[v] = i

    for movie in movies:
        actor_list = sorted(movies[movie])
        for i in actor_list:
            for j in actor_list:
                #if i != j: discussion, how to consider the coactor relationship with the actor himself/herself?
                    coactor_matrix[actors_index[i]][actors_index[j]] += 1

    return coactor_matrix, actors, actors_index


def build_movie_movie_matrix(movies_tag_vector):
    """
    Build movie similarity matrix based on the movies_tag_vector.
    :param movies_tag_vector: movies in tags vector space
    :return: movie_matrix, 2D matrix describes the movie similarity
             movies, sorted_movie_list, you can get (index in matrix)->(movie id)
             movies_index, movie_index_dict, you can get (movie id)->(index in matrix)
    """

    movies = sorted(movies_tag_vector.keys())
    movies_index = {}

    for i, v in enumerate(movies):
        movies_index[v] = i

    movie_matrix = zeros((len(movies), len(movies)))
    for i, _movie in enumerate(movies):
        for j, movie in enumerate(movies):
            movie_matrix[i][j] = dot_product(movies_tag_vector[_movie], movies_tag_vector[movie])[0]

    return movie_matrix, movies, movies_index


def build_movie_tag_matrix(all_movies, tags, movie_tag_vector):
    movie_tag_matrix = zeros(shape=(len(all_movies), len(tags)))
    for i in range(len(all_movies)):
        for j in range(len(tags)):
            if tags[j] in movie_tag_vector[all_movies[i]]:
                movie_tag_matrix[i][j] = movie_tag_vector[all_movies[i]][tags[j]]
    # redefined_matrix = []
    # redefined_movie_index = []
    # for i in range(len(movie_tag_matrix)):
    #     if sum(movie_tag_matrix[i]) != 0:
    #         redefined_matrix.append(movie_tag_matrix[i])
    #         redefined_movie_index.append(all_movies[i])
    return all_movies, movie_tag_matrix

  
def build_adjacency_matrix_from_adjacency_dict(dict):
    """
    Build a adjacency matrix using adjacency dict.
    :param dict: adjacency dict in {'key1': {'value1': value1, 'value2': value2}, 'key2': {'value1': value1}} format
    :return: adjacency matrix based on the adjacency dict.
             keys, sorted_key_list, you can get (row index in matrix)->(movie id in dict)
             keys_index, key_index_dict, you can get (movie id in dict)->(row index in matrix)
             values, sorted_value_list, you can get (column index in matrix)->(tag id in dict)
             values_index, value_index_dict, you can get (tag id in dict)->(column index in matrix)
    """
    keys = sorted(dict.keys())
    keys_index = {}
    values = set()
    values_index = {}

    for item in dict:
        for value in item:
            values.add(value[0])

    values = sorted(values)

    matrix = zeros((len(keys), len(values)))
    for i, key in enumerate(keys):
        keys_index[key] = i
        for j, value in enumerate(values):
            values_index[value] = j
            if key in dict and value in dict[key]:
                matrix[i][j] = dict[key][value]

    return matrix, keys, keys_index, values, values_index
