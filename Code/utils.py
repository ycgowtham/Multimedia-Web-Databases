##
# @brief CSE515-Fall2017 Project1
# @author Xiangyu Guo
# @date Sept 9, 2017
import csv
import math
import time
import os
import json

from collections import OrderedDict

class Tags(object):
    """docstring for Tags"""
    def __init__(self, record):
        super(Tags, self).__init__()
        self.tagid = int(record['tagId'])
        self.tag = record['tag']

    def __str__(self):
        return "{0} {1}".format(self.tagid, self.tag)

    def __getitem__(self, item):
        if item == 'tagId':
            return self.tagid
        if item == 'tag':
            return self.tag


class ActorInfo(object):
    """docstring for ActorInfo"""
    def __init__(self, record):
        super(ActorInfo, self).__init__()
        self.id = int(record['id'])
        self.name = record['name']

    def __str__(self):
        return "{0} {1}".format(self.id, self.name)

    def __getitem__(self, item):
        if item == 'id':
            return self.id
        if item == 'name':
            return self.name


class MovieInfo(object):
    """docstring for MovieInfo"""
    def __init__(self, record):
        super(MovieInfo, self).__init__()
        self.year = int(record['year'])
        self.movieid = int(record['movieid'])
        self.moviename = record['moviename']
        self.genres = record['genres']

    def __str__(self):
        return "{0} {1} {2} {3}".format(self.year, self.movieid, self.moviename, self.genres)

    def __getitem__(self, item):
        if item == 'year':
            return self.year
        if item == 'movieid':
            return self.movieid
        if item == 'moviename':
            return self.moviename
        if item == 'genres':
            return self.genres


class MovieActor(object):
    """docstring for MovieActor"""
    def __init__(self, record):
        super(MovieActor, self).__init__()
        self.actorid = int(record['actorid'])
        self.movieid = int(record['movieid'])
        self.actor_movie_rank = int(record['actor_movie_rank'])

    def __str__(self):
        return "{0} {1} {2}".format(self.actorid, self.movieid, self.actor_movie_rank)

    def __getitem__(self, item):
        if item == 'actorid':
            return self.actorid
        if item == 'movieid':
            return self.movieid
        if item == 'actor_movie_rank':
            return self.actor_movie_rank


class TagInfo(object):
    """docstring for TagInfo"""
    def __init__(self, record):
        super(TagInfo, self).__init__()
        self.userid = int(record['userid'])
        self.movieid = int(record['movieid'])
        self.tagid = int(record['tagid'])
        self.timestamp = int(time.mktime(time.strptime(record['timestamp'], "%Y-%m-%d %H:%M:%S")))

    def __str__(self):
        return "{0} {1} {2} {3}".format(self.userid, self.movieid, self.tagid, self.timestamp)

    def __getitem__(self, item):
        if item == 'userid':
            return self.userid
        if item == 'movieid':
            return self.movieid
        if item == 'tagid':
            return self.tagid
        if item == 'timestamp':
            return self.timestamp


class RatingInfo(object):
    """docstring for RatingInfo"""
    def __init__(self, record):
        super(RatingInfo, self).__init__()
        self.userid = int(record['userid'])
        self.movieid = int(record['movieid'])
        self.rating = int(record['rating'])
        self.timestamp = int(time.mktime(time.strptime(record['timestamp'], "%Y-%m-%d %H:%M:%S")))

    def __str__(self):
        return "{0} {1} {2} {3}".format(self.userid, self.movieid, self.rating, self.timestamp)

    def __getitem__(self, item):
        if item == 'userid':
            return self.userid
        if item == 'movieid':
            return self.movieid
        if item == 'rating':
            return self.rating
        if item == 'timestamp':
            return self.timestamp


def tags_adapter(record):
    record['tagId'] = int(record['tagId'])
    return record


def actor_info_adapter(record):
    record['id'] = int(record['id'])
    return record


def movie_info_adapter(record):
    record['year'] = int(record['year'])
    record['movieid'] = int(record['movieid'])
    return record


def movie_actor_adapter(record):
    record['actorid'] = int(record['actorid'])
    record['movieid'] = int(record['movieid'])
    record['actor_movie_rank'] = int(record['actor_movie_rank'])
    return record


def tag_adapter(record):
    record['userid'] = int(record['userid'])
    record['movieid'] = int(record['movieid'])
    record['tagid'] = int(record['tagid'])
    record['timestamp'] = int(time.mktime(time.strptime(record['timestamp'], "%Y-%m-%d %H:%M:%S")))
    return record

def rating_adapter(record):
    record['userid'] = int(record['userid'])
    record['movieid'] = int(record['movieid'])
    record['rating'] = int(record['rating'])
    record['timestamp'] = int(time.mktime(time.strptime(record['timestamp'], "%Y-%m-%d %H:%M:%S")))
    return record

def load_data(data_path, CC=None, total=None):
    """
    load csv data into the memory
    :param data_path: path to the csv file
    :return: loaded list
    """
    data = []
    fileName = os.path.basename(data_path)
    start = time.time()
    with open(data_path) as csvfile:
        reader = csv.DictReader(csvfile)
        for i, row in enumerate(reader):
            if CC is not None:
                data.append(CC(row))
            else:
                data.append(row)
            if total is not None and i % 1000 == 0:
                print_status(i, total, fileName)
        if total is not None:
            print_status(total, total, fileName)
    elapsed_time = time.time() - start
    print("\nFile {0} loaded in {1} seconds".format(fileName, elapsed_time))
    return data

def print_status(i, total, objName):
    pct = 100 * (i / total)
    print("\rCurrently processing object {0}: {1:.3f}%".format(objName, pct), end='', flush=True)

def convert_timestamp(vector, index):
    """
    convert timestamp from string to integer, and find the min/max of timestamps.
    :param vector: tags vector
    :param index: timestamps index
    :return: converted vector, and g_min_ts/g_max_ts as the minimum/maximum of the timestamp
    """

    # for row in vector:
    #    row[index] = int(time.mktime(time.strptime(row[index], "%Y-%m-%d %H:%M:%S")))

    return min(vector, key=lambda x: x[index])[index], max(vector, key=lambda x: x[index])[index]


def get_moive_actor_list(movie_actor):
    movie_actor_list = {}
    for m in movie_actor:
        movie_id = m['movieid']
        if movie_id not in movie_actor_list:
            movie_actor_list[movie_id] = set()
        movie_actor_list[movie_id].add(m['actorid'])
    return movie_actor_list


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
        value_1 = v1[key] if key in v1 else 0
        value_2 = v2[key] if key in v2 else 0

        a += value_1 ** 2
        b += value_2 ** 2
        p += value_1 * value_2

    if a == 0 or b == 0:
        return 0, 0

    cosine = p / (math.sqrt(a) * math.sqrt(b))

    return p, cosine


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


def tag_generator(mltags, key, movie_id, weight_func):
    """
    generate weighted tag vector for one movie
    :param mltags: tags table
    :param key: 'movieid'
    :param movie_id: movie id
    :param weight_func: apply weight on the count, customizable
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
                count = count + weight_func(actor_movie_rank, v)
            tag_vector[tag] = count
        return tag_vector

    return f


def actor_tag_vector(movie_actor, mltags, weight_func):
    """
    task one. print actor vector TF/ TF-IDF.
    :param movie_actor: movie actor table
    :param mltags: tags table
    :param weight_func: apply weight on the count, customizable
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
            calculator = tag_generator(mltags, 'movieid', last_movie_id, weight_func)

        for tag, value in calculator(int(movie['actor_movie_rank'])).items():
            if tag not in tags:
                tags[tag] = set()
            tags[tag].add(movie['actorid'])

            if tag in actors[movie['actorid']]:
                actors[movie['actorid']][tag] += value
            else:
                actors[movie['actorid']][tag] = value

    return tags, actors


def movie_actor_vector(movies, movie_actor, weight_func):
    """
    mapping movie into actor vector space
    :param movies: movies information table
    :param movie_actor: movies->actors relation
    :param weight_func: weight function to calculate tf-idf or raw count
    :return: features(actors) vector, movies actor
    """
    movie_year = {k['movieid']: k['year'] for k in movies}
    movies = {}
    actors = {}

    for movie in movie_actor:
        movieid = movie['movieid']
        actorid = movie['actorid']
        rank = int(movie['actor_movie_rank'])
        year = int(movie_year[movieid])
        if movieid not in movies:
            movies[movieid] = {}

        if actorid not in actors:
            actors[actorid] = set()

        movies[movieid][actorid] = weight_func(rank, year)
        actors[actorid].add(movieid)

    return actors, movies


def genre_tag_vector(movies, mltags, weight_func):
    """
    task two. print genre vector genre TF/ TF-IDF.
    :param movies: movies table
    :param mltags: tags table
    :param weight_func: apply weight on the count, customizable
    :return: features(tags) vector, genres vector.
    """
    genres = {}
    tags = {}

    for movie in movies:
        genre = str.split(movie['genres'], '|')
        movieid = movie['movieid']
        calculator = tag_generator(mltags, 'movieid', movieid, weight_func)

        for tag, value in calculator(1).items():
            for g in genre:
                if tag not in tags:
                    tags[tag] = set()
                tags[tag].add(g)

                if g not in genres:
                    genres[g] = {}
                if tag in genres[g]:
                    genres[g][tag] += value
                else:
                    genres[g][tag] = value

    return tags, genres


def movie_tag_vector(mlmovies, mltags, weight_func):
    """
    create a tag vector space and mapping movies into it.
    :param mlmovies: movies table
    :param mltags: tags table
    :param weight_func: apply weight on the count, customizable
    :return: features(tags) vector, genres vector.
    """
    movies = {}
    tags = {}

    objName = 'movie-tag vector'
    total_movies = len(mlmovies)

    for i, movie in enumerate(mlmovies):
        movieid = movie['movieid']
        movies[movieid] = {}
        calculator = tag_generator(mltags, 'movieid', movieid, weight_func)

        for tag, value in calculator(1).items():
            if tag not in tags:
                tags[tag] = set()
            tags[tag].add(movieid)
            if tag in movies[movieid]:
                movies[movieid][tag] += value
            else:
                movies[movieid][tag] = value

        if i % 100 == 0:
            print_status(i, total_movies, objName)

    print_status(total_movies, total_movies, objName)

    return tags, movies


def get_genre_movies_list(movies):
    """
    genreate moives list per genre
    :param movies: movies information table
    :return: movies list for each genre, min year of movies, max year of movies.
    """
    genres_movie_list = {}
    min_yr = 2017
    max_yr = 1900

    for movie in movies:
        genres = str.split(movie['genres'], '|')

        min_yr = min(min_yr, int(movie['year']))
        max_yr = max(max_yr, int(movie['year']))

        for g in genres:
            if g not in genres_movie_list:
                genres_movie_list[g] = set()
            genres_movie_list[g].add(movie['movieid'])

    return genres_movie_list, min_yr, max_yr


def get_users_watched_movies(mltags, mlratings):
    """
    get movies watched by the users from mltags and mlratings table
    :param mltags:
    :param mlratings:
    :return:
    """
    users = {}

    for record in mltags:
        if not record['userid'] in users:
            users[record['userid']] = set()

        users[record['userid']].add(record['movieid'])

    for record in mlratings:
        if not record['userid'] in users:
            users[record['userid']] = set()
        users[record['userid']].add(record['movieid'])

    return users


def get_moives_related_info(mlmovies, mlratings, movie_actor):
    movies = {}

    for record in mlmovies:
        movies[record['movieid']] = {}

    for record in movie_actor:
        movies[record['movieid']] = {}

    for record in mlratings:
        movies[record['movieid']] = {}

    for movie in movies:
        movies[movie]['ratings'] = []
        movies[movie]['year'] = '1900'
        movies[movie]['actors'] = set()
        movies[movie]['genres'] = set()

    for record in mlmovies:
        movies[record['movieid']]['year'] = record['year']
        genres = str.split(record['genres'], '|')
        for genre in genres:
            movies[record['movieid']]['genres'].add(genre)

    for record in movie_actor:
        movies[record['movieid']]['actors'].add(record['actorid'])

    for record in mlratings:
        movies[record['movieid']]['ratings'].append(int(record['rating']))

    return movies

class FileTotalManager:
    def getFileTotal(self, fileName):
        if fileName in self.fileTotals:
            return self.fileTotals[fileName]
        return None
    def readFile(self, fileName):
        try:
            with open(fileName) as f:
                return json.load(f)
        except FileNotFoundError as e:
            print("Failed to find file {0}".format(fileName))
        except:
            print("An error occured processing {0}".format(fileName))
        return {}
    def __init__(self, fileName):
        self.fileTotals = self.readFile(fileName)
