##
# @brief CSE515-Fall2017 Project1
# @author Xiangyu Guo
# @date Sept 9, 2017
import csv
import time
import math

g_min_ts = 0
g_max_ts = 0


def load_data(data_path):
    """
    load csv data into the memory
    :param data_path: path to the csv file
    :return: loaded list
    """
    data = []
    with open(data_path) as csvfile:
        reader = csv.DictReader(csvfile)
        for row in reader:
            data.append(row)
    return data


def convert_timestamp(vector, index):
    """
    convert timestamp from string to integer, and find the min/max of timestamps.
    :param vector: tags vector
    :param index: timestamps index
    :return: converted vector, and g_min_ts/g_max_ts as the minimum/maximum of the timestamp
    """
    global g_min_ts
    global g_max_ts

    for row in vector:
        row[index] = int(time.mktime(time.strptime(row[index], "%Y-%m-%d %H:%M:%S")))

    g_min_ts = min(vector, key=lambda x: x[index])[index]
    g_max_ts = max(vector, key=lambda x: x[index])[index]


def vector_diff(v1, v2):
    """
    Calculate the difference between two vectors
    :param v1: vector one
    :param v2: vector two
    :return: new vector, pointing from the end of vector two to vector one
    """
    diff = {}

    key_set = set(v1.keys()).union(v2.keys())
    for key in key_set:
        if key not in v1:
            v1[key] = 0
        if key not in v2:
            v2[key] = 0
        diff[key] = v1[key] - v2[key]

    return diff

def p_norms(v, p):
    """
    Calculate p-norms of the vector
    :param v: vector
    :param p: level of p, 'inf' means infinite norm.
    :return: p-norm value of these two vectors
    """
    diff = []
    func = sum
    if p == 'inf':
        p = 1
        func = max

    for key in v.keys():
        diff.append(math.pow(math.fabs(v[key]), p))

    return math.pow(func(diff), 1.0/p)


def dot_product(v1, v2):
    """
    Calculate dot product between two vectors
    :param v1: vector one
    :param v2: vector two
    :return: dot product, cosine value
    """
    p = 0
    a = 0
    b = 0
    key_set = set(v1.keys()).union(v2.keys())
    for key in key_set:
        if key not in v1:
            v1[key] = 0
        if key not in v2:
            v2[key] = 0
        a += v1[key] * v1[key]
        b += v2[key] * v2[key]
        p += v1[key] * v2[key]

    cosine = p / (math.sqrt(a) * math.sqrt(b))

    return p, cosine


def weight(mr, ts):
    """
    weight function.
    :param mr: movie rank
    :param ts: timestamp
    :return: (1 / rank) * ( TS - min(TS) + 1) / (max(TS)- min(TS) + 1)
    """
    global g_min_ts
    global g_max_ts
    return (1.0 / mr) * (ts - g_min_ts + 1) / (g_max_ts - g_min_ts + 1)


def tf_idf(vector, idf, models):
    """
    calculate tf or tf-idf of a given vector
    :param vector: objects document
    :param idf: idf value of this document
    :param models: 'tf' or 'tf-idf'
    :return: result vector
    """
    total = sum(vector.values())
    if models == 'tf-idf':
        return {k: v/total * idf[k] for k, v in vector.items()}
    elif models == 'tf':
        return {k: v/total for k, v in vector.items()}
    return {}


def idf(document, features):
    """
    calculate idf of a given document
    :param document: objects document
    :param features: how many objects contain the feature
    :return: idf vector of each feature in the document
    """
    n = len(document)
    return {k: math.log(n/len(v)) for k, v in features.items()}


def tf_idf_diff(g1, g2, movies_per_genres, tags_per_movie, genres):
    """
    calculate tf-idf-diff between genre1 and genre2, explain the difference using p-norm and dot product
    :param g1: genre one
    :param g2: genre two
    :param movies_per_genres: movie set of each genre
    :param tags_per_movie: tag set of each movie
    :param genres: tag vector of each genre
    :return: NULL
    """
    if (g1 not in movies_per_genres) or (g2 not in movies_per_genres):
        return {}
    tags = {}

    movies = movies_per_genres[g1].union(movies_per_genres[g2])

    for movie in movies:
        for tag in tags_per_movie[movie]:
            if tag not in tags:
                tags[tag] = set()
            tags[tag].add(movie)

    g1_vector = tf_idf(genres[g1], idf(movies, tags), 'tf-idf')
    g2_vector = tf_idf(genres[g2], idf(movies, tags), 'tf-idf')
    diff = vector_diff(g1_vector, g2_vector)

    print('1-Norm = ', p_norms(diff, 1))
    print('2-Norm = ', p_norms(diff, 2))
    print('3-Norm = ', p_norms(diff, 3))
    print('Infinity-Norm = ', p_norms(diff, 'inf'))
    p, cosine = dot_product(g1_vector, g2_vector)
    print('Dot product = ', p)
    print('Cosine = ', cosine)
    print('Angle = ', math.acos(cosine)/math.pi * 180)
    return diff


def p_diff(g1, g2, movies_per_genres, tags_per_movie, mode):
    """
    calculate p-diff1 or p-diff2 between genre1 and genre2.
    :param g1: genre one
    :param g2: genre two
    :param movies_per_genres: movie set of each genre
    :param tags_per_movie: tag set of each movie
    :param mode: '1':p-diff1, '2':p-diff2
    :return: NULL
    """
    if (g1 not in movies_per_genres) or (g2 not in movies_per_genres):
        return

    diff = {}
    movies = movies_per_genres[g1].union(movies_per_genres[g2])
    g = g1
    if mode == '2':
        g = g2
    R = len(movies_per_genres[g])
    M = len(movies)

    for movie in movies:
        for tag in tags_per_movie[movie]:
            if tag not in diff:
                diff[tag] = {}
                diff[tag]['r'] = 0
                diff[tag]['m'] = 0
            diff[tag]['m'] += 1
            if movie in movies_per_genres[g]:
                diff[tag]['r'] += 1

    if mode == '2':
        for tag in diff:
            diff[tag]['r'] = R - diff[tag]['r']
            diff[tag]['m'] = M - diff[tag]['m']

    # print(R, M)
    for tag in diff:
        m = diff[tag]['m']
        r = diff[tag]['r']
        # print(diff[tag]['r'], diff[tag]['m'])
        n_offset = 0
        d_offset = 0
        fixed_correction = 0.5 # or m / M on R = 0
        if (M == R) or (m == r) or (R == r) or (r == 0) or (M - m - R + r == 0):
            d_offset = 1
            n_offset = d_offset - fixed_correction

        diff[tag] = math.log(((r + n_offset) * (M - m - R + r + n_offset)) / ((R - r + n_offset) * (m - r + n_offset))) * \
                    math.fabs(((r + n_offset) / (R + d_offset)) - ((m - r + n_offset) / (M - R + d_offset)))

    return diff


def tag_generator(mltags, key, movie_id):
    """
    generate weighted tag vector for one movie
    :param mltags: tags table
    :param key: 'movieid'
    :param movie_id: movie id
    :return: a function will take movie rank as the weight
    """
    ts = {}
    for record in mltags:
        if record[key] == movie_id:
            if record['tagid'] not in ts:
                ts[record['tagid']] = []
            ts[record['tagid']].append(int(record['timestamp']))

    def f(actor_movie_rank):
        tag_vector = {}
        for tag, value in ts.items():
            count = 0
            for v in value:
                count = count + weight(actor_movie_rank, v)
            tag_vector[tag] = count
        return tag_vector

    return f


def actor_tag_vector(movie_actor, mltags):
    """
    task one. print actor vector TF/ TF-IDF.
    :param movie_actor: movie actor table
    :param mltags: tags table
    :return: features(tags) vector, actors vector
    """
    actors = {}
    tags = {}

    # tags sort by movie_id, performance improvement
    # mltags = sorted(mltags, key=lambda x: x['movieid'])

    last_movie_id = 0
    # go through movie_actor, get actorid, actor_movie_rank
    for movie in movie_actor:
        if movie['actorid'] not in actors:
            actors[movie['actorid']] = {}

        if last_movie_id != movie['movieid']:
            last_movie_id = movie['movieid']
            calculator = tag_generator(mltags, 'movieid', last_movie_id)

        for tag, value in calculator(int(movie['actor_movie_rank'])).items():
            if tag not in tags:
                tags[tag] = set()
            tags[tag].add(movie['actorid'])

            if tag in actors[movie['actorid']]:
                actors[movie['actorid']][tag] += value
            else:
                actors[movie['actorid']][tag] = value

    return tags, actors


def genre_tag_vector(movies, mltags):
    """
    task two. print genre vector genre TF/ TF-IDF.
    :param movies: movies table
    :param mltags: tags table
    :return: features(tags) vector, genres vector, movie set of each genre, tag set of each movie.
    """
    genres = {}
    tags = {}
    movies_per_genres = {}
    tags_per_movies = {}

    for movie in movies:
        genre = str.split(movie['genres'], '|')
        movieid = movie['movieid']
        calculator = tag_generator(mltags, 'movieid', movieid)
        if movieid not in tags_per_movies:
            tags_per_movies[movieid] = {}

        for tag, value in calculator(1).items():
            for g in genre:
                if tag not in tags:
                    tags[tag] = set()
                tags[tag].add(g)

                if g not in movies_per_genres:
                    movies_per_genres[g] = set()
                movies_per_genres[g].add(movieid)

                if g not in genres:
                    genres[g] = {}
                if tag in genres[g]:
                    genres[g][tag] += value
                else:
                    genres[g][tag] = value

                if tag in tags_per_movies[movieid]:
                    tags_per_movies[movieid][tag] += value
                else:
                    tags_per_movies[movieid][tag] = value

    return tags, genres, movies_per_genres, tags_per_movies


def user_tag_vector(mlratings, mltags):
    """
    task three. print user vector userID TF/ TF-IDF.
    :param mlratings: ratings table
    :param mltags: tags table
    :return: features(tags) vector, user vector
    """
    users = {}
    tags = {}
    calculators = {}

    for record in mltags:
        users[record['userid']] = {}

    for record in mlratings:
        users[record['userid']] = {}

    for record in mltags:
        calculators[record['movieid']] = {}

    for cal in calculators:
        calculators[cal] = tag_generator(mltags, 'movieid', cal)

    def f(r):
        user = r['userid']
        movie = r['movieid']
        if movie not in calculators:
            return
        for tag, value in calculators[movie](1).items():
            if tag not in tags:
                tags[tag] = set()
            tags[tag].add(user)

            if tag in users[user]:
                users[user][tag] += value
            else:
                users[user][tag] = value

    for record in mltags:
        f(record)

    for record in mlratings:
        f(record)

    return tags, users


def main():
    # load data into memory
    print('loading csv data into memroy...')

    movie_actor = load_data('../data/movie-actor.csv')
    mltags = load_data('../data/mltags.csv')
    tags = load_data('../data/genome-tags.csv')
    mlmovies = load_data('../data/mlmovies.csv')
    # mlusers = load_data('../data/mlusers.csv')
    mlratings = load_data('../data/mlratings.csv')

    print('loading completed!')
    # print(movie_actor[0].keys(), mltags[0].keys(), tags[0].keys(), mlmovies[0].keys(), mlusers[0].keys())
    print('preprocessing data...')

    # conversion
    convert_timestamp(mltags, 'timestamp')
    tags = {k['tagId']: k['tag'] for k in tags}

    # preprocessing
    actors_idf, actors = actor_tag_vector(movie_actor, mltags)
    actors_idf = idf(actors, actors_idf)
    genres_idf, genres, movies_per_genres, tags_per_movies = genre_tag_vector(mlmovies, mltags)
    genres_idf = idf(genres, genres_idf)
    users_idf,  users = user_tag_vector(mlratings, mltags)
    users_idf = idf(users, users_idf)

    tasks = {'print_actor_vector': [actors, actors_idf],
             'print_genre_vector': [genres, genres_idf],
             'print_user_vector':  [users, users_idf]}

    task4 = {'tf-idf-diff': [tf_idf_diff, genres],
             'p-diff1': [p_diff, '1'],
             'p-diff2': [p_diff, '2']}

    print('preprocessing completed!')

    # start query
    while True:
        command_line = input('query>')
        commands = command_line.split(' ')

        if len(commands) == 3 and commands[0] in tasks and \
                commands[1] in tasks[commands[0]][0]:
            result = sorted(tf_idf(tasks[commands[0]][0][commands[1]], tasks[commands[0]][1], commands[2]).items(),
                            key=lambda x: x[1], reverse=True)
            for r in result:
                print(tags[r[0]], r[1])

        if len(commands) == 4 and commands[0] == "differentiate_genre" and \
                commands[3] in task4:
            result = task4[commands[3]][0](commands[1], commands[2], movies_per_genres, tags_per_movies, task4[commands[3]][1])
            result = sorted(result.items(), key=lambda x: (x[1], x[0]), reverse=True)
            for r in result:
                print(tags[r[0]], r[1])

    # actors['1582699']
    # genres['Thriller']
    # users['146']

    # g1 = 'Thriller'
    # g2 = 'Romance'

if __name__ == '__main__':
    main()
