from numpy import mean, zeros
import utils

def build_actor_movie_year_tensor(movie_actor_table, mlmovies_table):
    """
    Build a actor-movie-year tensor using movie-actor and mlmovies
    :param movie_actor_table:
    :param mlmovies_table:
    :return: tensor, 3D tensor describes the actor-movie-year relationships
             actors, sorted_actors_list, you can get (index in tensor)->(actor id)
             actors_index, actors_index_dict, you can get (actor id)->(index in tensor)
             movies, sorted_movies_list, you can get (index in tensor)->(movie id)
             movies_index, movies_index_dict, you can get (movie id)->(index in tensor)
             years, sorted_years_list, you can get (index in tensor)->(year)
             years_index, years_index_dict, you can get (year)->(index in tensor)
    """
    movies = {}
    movies_index = {}
    actors = set()
    actors_index = {}
    years = set()
    years_index = {}

    for record in movie_actor_table:
        if not record['movieid'] in movies:
            movies[record['movieid']] = set()

        movies[record['movieid']].add(record['actorid'])
        actors.add(record['actorid'])

    for record in mlmovies_table:
        years.add(record['year'])

    actors = sorted(actors)
    for i, v in enumerate(actors):
        actors_index[v] = i

    years = sorted(years)
    for i, v in enumerate(years):
        years_index[v] = i

    for i, v in enumerate(sorted(movies.keys())):
        movies_index[v] = i

    tensor = zeros((len(actors), len(movies), len(years)))
    for record in mlmovies_table:
        movieid = record['movieid']
        j = movies_index[movieid]
        k = years_index[record['year']]
        for actor in movies[movieid]:
            i = actors_index[actor]
            tensor[i][j][k] = 1

    return tensor, (actors, actors_index, movies, movies_index, years, years_index)


def build_tag_movie_rating_tensor(tag_list, mlratings_table):
    """
    Build a tag-movie-rating tensor using mltags and mlratings
    :param tag_list:
    :param mlratings_table:
    :return: tensor, 3D tensor describes the tag-movie-rating relationships
             tags, sorted_tags_list, you can get (index in tensor)->(tag id)
             tags_index, tags_index_dict, you can get (tag id)->(index in tensor)
             movies, sorted_movies_list, you can get (index in tensor)->(movie id)
             movies_index, movies_index_dict, you can get (movie id)->(index in tensor)
    """
    movies_ratings = {}
    movies = set()
    tags = set(tag_list)

    movies_index = {}
    tags_index = {}

    print("Scanning ratings...")
    total_len = len(mlratings_table)

    for i, record in enumerate(mlratings_table):
        movies.add(record['movieid'])

        if not record['movieid'] in movies_ratings:
            movies_ratings[record['movieid']] = []

        movies_ratings[record['movieid']].append(int(record['rating']))

        if i % 1000 == 0:
            utils.print_status(i, total_len, 'mlratings_table')
    utils.print_status(total_len, total_len, 'mlratings_table')

    print("\nCalculating averages...")
    total_len = len(movies_ratings)

    for i, movie in enumerate(movies_ratings):
        movies_ratings[movie] = mean(movies_ratings[movie])

        if i % 20 == 0:
            utils.print_status(i, total_len, 'movies_ratings')
    utils.print_status(total_len, total_len, 'movies_ratings')

    tensor = zeros((len(tags), len(movies_ratings), 6))

    tags = sorted(tags)
    movies = sorted(movies)

    print("\nGenerating tag indices...")
    total_len = len(tags)

    for i, v in enumerate(tags):
        tags_index[v] = i
        if i % 20 == 0:
            utils.print_status(i, total_len, 'tags')
    utils.print_status(total_len, total_len, 'tags')

    print("\nGenerating movie indices...")
    total_len = len(movies)

    for i, v in enumerate(movies):
        movies_index[v] = i
        if i % 20 == 0:
            utils.print_status(i, total_len, 'movies')
    utils.print_status(total_len, total_len, 'movies')

    print("\nGenerating tensor...")
    total_len = len(tags)

    for n, i in enumerate(tags):
        for j in movies:
            for k in range(6):
                if movies_ratings[j] <= k: # should it be smaller or greater? Check the MINC FAQ
                    tensor[tags_index[i]][movies_index[j]][k] = 1
        if n % 20 == 0:
            utils.print_status(n, total_len, 'tensor')
    utils.print_status(total_len, total_len, 'tensor')
    print()

    return tensor, (tags, tags_index, movies, movies_index, list(range(6)), list(range(6)))
