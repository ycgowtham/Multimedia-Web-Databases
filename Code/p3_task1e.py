from p3_task1a import recommender_system_using_svd_pca
from p3_task1b import recommender_system_using_lda
from p3_task1c import recommender_system_using_cp
from p3_task1d import recommender_system_using_ppr

def recommender_system_combining_all(user_id,
                                     users_watched_movies,
                                     movies_tfidf_tags_vector,
                                     movies_tags_vector,
                                     tag_movie_rating,
                                     tmr_tensor_info,
                                     genome_tags):

    """
    p3_task1e 1151
    :param user_id:
    :param users_watched_movies:
    :param movies_tfidf_tags_vector:
    :param movies_tags_vector:
    :param tag_movie_rating:
    :param tmr_tensor_info:
    :param genome_tags:
    :return:
    """

    pca = recommender_system_using_svd_pca(user_id,
                                           users_watched_movies,
                                           movies_tfidf_tags_vector,
                                           genome_tags,
                                           "PCA")

    svd = recommender_system_using_svd_pca(user_id,
                                           users_watched_movies,
                                           movies_tfidf_tags_vector,
                                           genome_tags,
                                           "SVD")

    lda = recommender_system_using_lda(user_id,
                                       users_watched_movies,
                                       movies_tags_vector,
                                       genome_tags)

    cp = recommender_system_using_cp(user_id,
                                     users_watched_movies,
                                     movies_tags_vector,
                                     tag_movie_rating,
                                     tmr_tensor_info,
                                     genome_tags)

    ppr = recommender_system_using_ppr(user_id,
                                       users_watched_movies,
                                       movies_tfidf_tags_vector)


    pca_total = get_total_weight(pca)
    svd_total = get_total_weight(svd)
    lda_total = get_total_weight(lda)
    cp_total = get_total_weight(cp)
    ppr_total = get_total_weight(ppr)

    combination = {}
    combination = get_combinations(combination, pca, pca_total)
    combination = get_combinations(combination, svd, svd_total)
    combination = get_combinations(combination, lda, lda_total)
    combination = get_combinations(combination, cp, cp_total)
    combination = get_combinations(combination, ppr, ppr_total)

    combination = sorted(combination.items(), key= lambda x: x[1], reverse=True)

    return combination

def get_total_weight(model):
    return sum(wt for mv, wt in model)

def get_combinations(combination, model, total_weight):
    for movie, wt in model:
        set_movie_weight(combination, movie, wt/total_weight)
    return combination

def set_movie_weight(combination, movie, weight):
    prev_weight = 0
    if movie in combination:
        prev_weight = combination[movie]
    combination[movie] = prev_weight + weight
    return combination
