import utils as base
from matrix_builder import *
from tensor_builder import *

from p3_task1a import recommender_system_using_svd_pca
from p3_task1b import recommender_system_using_lda
from p3_task1c import recommender_system_using_cp
from p3_task1d import recommender_system_using_ppr
from p3_task1e import recommender_system_combining_all


from p3_task2 import gen_prob_feedback_function

from recommendation_tools import WeightConstants, print_output_using
from p3_task5 import recommender_system_for_labeling_movies
from p3_task3 import lsh_indexing
from p3_task5 import header

def help():
    print('Supported commands:')
    print('p3_task1a <user_id> <PCA|SVD> [pf]')
    print('p3_task1b <user_id> [pf]')
    print('p3_task1c <user_id> [pf]')
    print('p3_task1d <user_id> [pf]')
    print('p3_task1e <user_id> [pf]')
    print('p3_task3 <l> <k>')
    print('p3_task5 <NN | DT | SVM> <r>')
    print('reset wc')

def main():
    fileTotalManager = base.FileTotalManager('./file_lengths.json')
    # load data into memory
    print('loading csv data into memory...')

    genome_tags = base.load_data('../data/genome-tags.csv',
                                 base.tags_adapter,
                                 fileTotalManager.getFileTotal('genome-tags.csv'))

    movies_info = base.load_data('../data/mlmovies.csv',
                                 base.movie_info_adapter,
                                 fileTotalManager.getFileTotal('mlmovies.csv'))

    ratings_info = base.load_data('../data/mlratings.csv',
                                  base.RatingInfo,
                                  fileTotalManager.getFileTotal('mlratings.csv'))

    tags_info = base.load_data('../data/mltags.csv',
                               base.TagInfo,
                               fileTotalManager.getFileTotal('mltags.csv'))

    print('loading completed!')

    # print(movie_actor[0].keys(), mltags[0].keys(), tags[0].keys(), mlmovies[0].keys(), mlusers[0].keys())
    print('preprocessing data...')

    # conversion
    min_ts, max_ts = base.convert_timestamp(tags_info, 'timestamp')
    # base.convert_timestamp(ratings_info, 'timestamp')
    genome_tags = {k['tagId']: k['tag'] for k in genome_tags}
    # movie_actor_list = base.get_moive_actor_list(movie_actor)
    # genres_movie_list, min_yr, max_yr = base.get_genre_movies_list(movies_info)
    movie_names = {k['movieid']: k['moviename'] for k in movies_info}
    # actor_names = {k['id']: k['name'] for k in actor_info}

    def tfidf_tag_weight(mr, ts):
        return (1.0 / mr) * (ts - min_ts + 1) / (max_ts - min_ts + 1)

    def no_weight(mr, ts):
        return 1

    print('building vectors')
    # actor_tags_vector
    # actors_tags_vector = base.actor_tag_vector(movie_actor, tags_info, no_weight)[1]
    # actors_idf, actors_tfidf_tags_vector = base.actor_tag_vector(movie_actor, tags_info, tfidf_tag_weight)
    # actors_idf = base.idf(actors_tfidf_tags_vector, actors_idf)
    # for actor in actors_tfidf_tags_vector.keys():
    #     actors_tfidf_tags_vector[actor] = base.tf_idf(actors_tfidf_tags_vector[actor], actors_idf, 'tf-idf')

    # movie_tags_vector
    print('Building standard movie-tag vector')
    movies_tags_vector = base.movie_tag_vector(movies_info, tags_info, no_weight)[1]

    print('\nBuilding tf-idf movie-tag vector')
    movies_idf, movies_tfidf_tags_vector = base.movie_tag_vector(movies_info, tags_info, tfidf_tag_weight)
    movies_idf = base.idf(movies_tfidf_tags_vector, movies_idf)
    for i, movie in enumerate(movies_tfidf_tags_vector.keys()):
        movies_tfidf_tags_vector[movie] = base.tf_idf(movies_tfidf_tags_vector[movie], movies_idf, 'tf-idf')

    # movie_actors_vector
    # movies_actors_vector = base.movie_actor_vector(movies_info, movie_actor, no_weight)[1]
    # movies_actor_idf, movies_tfidf_actors_vector = base.movie_actor_vector(movies_info, movie_actor, tfidf_actor_weight)
    # movies_actor_idf = base.idf(movies_tfidf_actors_vector, movies_actor_idf)
    # for movie in movies_tfidf_actors_vector.keys():
    #     movies_tfidf_actors_vector[movie] = base.tf_idf(movies_tfidf_actors_vector[movie], movies_actor_idf, 'tf-idf')

    # create actor-actor matrix
    # actor_actor_similarity, actors_list, actors_index = build_actor_actor_matrix(actors_tfidf_tags_vector)

    # create coactor-coactor matrix
    # coactor_coactor_matrix, coactors_list, coactors_index = build_coactor_coactor_matrix(movie_actor)

    # print('building AMY tensor')
    # create Actor-Movie-Year tensor (AMY tensor)
    # actor_movie_year_tensor, amy_tensor_info = build_actor_movie_year_tensor(movie_actor, movies_info)

    print('\nbuilding TMR tensor')
    # create Tag-Movie-Rating tensor (TMR tensor)
    tag_movie_rating, tmr_tensor_info = build_tag_movie_rating_tensor(genome_tags.keys(), ratings_info)

    print('creating list')
    # create watched list
    users_watched_movies = base.get_users_watched_movies(tags_info, ratings_info)

    # create watched movies info
    # watched_movies_info = base.get_moives_related_info(movies_info, ratings_info, movie_actor)

    print('preprocessing completed!')

    while True:
        command_line = input('query>')
        commands = command_line.split(' ')
        relevance_feedback = None

        if len(commands) > 0 and 'p3_task1' in commands[0]:
            if len(commands) == 3:
                if commands[2] == 'pf':
                    relevance_feedback = gen_prob_feedback_function(movies_tags_vector)
                else:
                    if not (commands[2] == 'PCA' or commands[2] == 'SVD'):
                        help()
                        continue
            elif len(commands) == 4:
                if commands[3] == 'pf':
                    relevance_feedback = gen_prob_feedback_function(movies_tags_vector)
                else:
                    help()
                    continue

            WeightConstants.initialize(movie_names,
                                       tags_info,
                                       ratings_info)

        if commands[0] == 'p3_task1a' and len(commands) > 2:
            user_id = int(commands[1])

            similarities = recommender_system_using_svd_pca(user_id,
                                                            users_watched_movies,
                                                            movies_tfidf_tags_vector,
                                                            genome_tags,
                                                            commands[2])

            print_output_using(user_id, similarities, relevance_feedback)

        elif commands[0] == 'p3_task1b' and len(commands) > 1:
            user_id = int(commands[1])

            similarities = recommender_system_using_lda(user_id,
                                                        users_watched_movies,
                                                        movies_tags_vector,
                                                        genome_tags)

            print_output_using(user_id, similarities, relevance_feedback)

        elif commands[0] == 'p3_task1c' and len(commands) > 1:
            user_id = int(commands[1])

            similarities = recommender_system_using_cp(user_id,
                                                       users_watched_movies,
                                                       movies_tags_vector,
                                                       tag_movie_rating,
                                                       tmr_tensor_info,
                                                       genome_tags)

            print_output_using(user_id, similarities, relevance_feedback)

        elif commands[0] == 'p3_task1d' and len(commands) > 1:
            user_id = int(commands[1])

            similarities = recommender_system_using_ppr(user_id,
                                         users_watched_movies,
                                         movies_tfidf_tags_vector)

            print_output_using(user_id, similarities, relevance_feedback)

        elif commands[0] == 'p3_task1e' and len(commands) > 1:
            user_id = int(commands[1])

            similarities = recommender_system_combining_all(user_id,
                                                            users_watched_movies,
                                                            movies_tfidf_tags_vector,
                                                            movies_tags_vector,
                                                            tag_movie_rating,
                                                            tmr_tensor_info,
                                                            genome_tags)

            print_output_using(user_id, similarities, relevance_feedback)

        elif commands[0] == 'p3_task3' and len(commands) == 3:
            lsh_indexing(genome_tags, movie_names, movies_tags_vector, int(commands[1]), int(commands[2]))
        elif commands[0] == 'p3_task5' and len(commands) > 1:
            labelled_movies = {}
            n = int(input("Enter number of labels: "))
            while (n > 0):
                label = input("Enter label: ")
                movie_data = input("Enter space separated movies for label "
                                   "" + label + ": ")
                movies = movie_data.split(" ")
                for i, m in enumerate(movies):
                    movies[i] = int(m)
                labelled_movies[label] = movies
                n -= 1

            if commands[1] == 'NN' and len(commands) > 2:
                recommender_system_for_labeling_movies(movies_info,
                                                       labelled_movies,
                                                       genome_tags,
                                                       movies_tfidf_tags_vector,
                                                       commands[1],
                                                       int(commands[2]))
            elif commands[1] == 'SVM' or commands[1] == 'DT':
                recommender_system_for_labeling_movies(movies_info,
                                                       labelled_movies,
                                                       genome_tags,
                                                       movies_tfidf_tags_vector,
                                                       commands[1], 0)
        elif len(commands) > 1 and (commands[0] == 'reset' and
                                    commands[1] == 'wc'):
            WeightConstants.reset()
            print("WeightConstants data has been purged")
        else :
            help()

if __name__ == '__main__':
    main()
