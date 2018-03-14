import sys
import numpy as np

from matrix_builder import build_movie_tag_matrix
from hash_family import HashFamily


def lsh_forest(candidate_movies, hash_table):
    for k in hash_table.keys():
        if type(k) == int:
            candidate_movies.add(k)
        else:
            lsh_forest(candidate_movies, hash_table[k])


def print_output(movie_names, vector):
    for i, v in enumerate(vector):
        print('{0} {1} - similarity {2}'.format(i + 1, movie_names[v[0]], v[1]))


def lsh_indexing(genome_tags, movie_names, D, L, K):
    # load data, big data set D.
    # convert d dimensions tags to 500 dimensions latent features.
    # U, c, V = svd(D, 500)
    print("Performing SVD on movie-tag matrix")
    tags = sorted(genome_tags.keys())
    all_movies = sorted(D.keys())
    all_movies, movie_tag_matrix = build_movie_tag_matrix(all_movies, tags, D)
    movie_index = {}
    n = len(all_movies)

    U, S, VT = np.linalg.svd(movie_tag_matrix, full_matrices=False)

    hash_funcs = [[None for i in range(K)] for i in range(L)]
    for i in range(L):
        for j in range(K):
            hash_funcs[i][j] = HashFamily(n, len(U[0][:500]))

    print("Creating hash table...")
    # create hash files(in memory or on disk)
    hash_tables = [{} for i in range(L)]
    for index, movie_id in enumerate(all_movies):
        for i in range(L):
            hash_table = hash_tables[i]
            for j in range(K):
                hash_value = hash_funcs[i][j].compute_hash(U[index][:500])
                if hash_value not in hash_table:
                    hash_table[hash_value] = {}
                hash_table = hash_table[hash_value]
            hash_table[movie_id] = 1
            movie_index[movie_id] = index

        percent = float(index) / n
        arrow = '-' * int(round(percent * 50)-1) + '>'
        spaces = ' ' * (50 - len(arrow))

        sys.stdout.write("\rPercent: [{0}] {1}%".format(arrow + spaces, int(round(percent * 100))))
        sys.stdout.flush()

    print()

    # search> movie_id range(r)
    while True:
        print('search for the movieid and range, "exit" to quit')
        search_conditions = input('search>')
        if search_conditions == 'exit':
            break

        search_conditions = search_conditions.split(' ')
        if len(search_conditions) != 2:
            continue

        if not search_conditions[0].isdigit():
            print('Invalid movie id')
            continue

        if int(search_conditions[0]) not in all_movies:
            print('No tags for such movie')
            continue

        if not search_conditions[1].isdigit():
            print('Range r should be numbers')
            continue

        movie_id = int(search_conditions[0])
        search_range = int(search_conditions[1])
        center_point = U[movie_index[movie_id]][:500].copy()

        while True:
            m = K
            candidate_movies = set()

            while m >= 0:
                for i in range(L):
                    hash_table = hash_tables[i]
                    for j in range(m):
                        hash_value = hash_funcs[i][j].compute_hash(center_point)
                        if hash_value not in hash_table:
                            break
                        hash_table = hash_table[hash_value]

                    lsh_forest(candidate_movies, hash_table)
                    if len(candidate_movies) > search_range:
                        break

                if len(candidate_movies) > search_range:
                    break
                m -= 1

            considered = len(candidate_movies)
            similarity = {}
            for movie in candidate_movies:
                if movie != movie_id:
                    similarity[movie] = np.dot(U[movie_index[movie]][:500], center_point)

            similarity = sorted(similarity.items(), key= lambda x: x[1], reverse=True)
            print_output(movie_names, similarity[:search_range])
            print('total movies considered {0}'.format(considered))

            improve = 'n'
            while True:
                improve = input('Do you want to improve the result? <y/n>')
                if improve in ('y', 'Y', 'n', 'N'):
                    break

            if improve in ('y', 'Y'):
                new_direction = np.zeros(len(U[0][:500]))
                relevent = input("\nPlease input relevant entries,between 1 and {0} ".format(search_range) +
                                        "separated by commas (e.g. 1,2,3) or 'q' to skip:\n")

                if relevent != 'q':
                    relevent = relevent.split(',')
                    for i in relevent:
                        j = similarity[int(i)][0]
                        new_direction = new_direction + U[movie_index[j]][:500] / 2

                irrelevant = input("\nPlease input irrelevant entries, between 1 and {0} ".format(search_range) +
                                   "separated by commas (e.g. 1,2,3) or 'q' to skip:\n")

                if irrelevant != 'q':
                    irrelevant = irrelevant.split(',')

                    for i in irrelevant:
                        j = similarity[int(i)][0]
                        new_direction = new_direction - U[movie_index[j]][:500] / 2

                center_point += new_direction
                print('Moving query point towards:', new_direction)
            else:
                break
