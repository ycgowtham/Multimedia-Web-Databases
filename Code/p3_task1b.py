import gensim, numpy, utils

from recommendation_tools import weigh_similarities

def recommender_system_using_lda(user_id,
                                 user_movie_map,
                                 movie_tag_frequency,
                                 genome_tags):
    user_movies = None
    try:
        user_movies = user_movie_map[user_id]
    except KeyError as e:
        print('User {0} does not exist!'.format(user_id))
        return

    user_tags = aggregate_tags_for_user(user_movies,
                                        movie_tag_frequency,
                                        genome_tags)

    models = generate_models(user_tags)

    similarities = find_similarities(models,
                                     user_movies,
                                     movie_tag_frequency)

    weighted_similarities = weigh_similarities(user_id,
                                               similarities)

    return weighted_similarities

def aggregate_tags_for_user(user_movies,
                            movie_tag_frequency,
                            genome_tags):
    user_movie_tags = {}

    for movie_id in movie_tag_frequency:
        for tag in genome_tags:
            if movie_id in user_movies:
                if movie_id not in user_movie_tags:
                    if len(user_movie_tags) > 100:
                        continue
                    user_movie_tags[movie_id] = {}

                if tag in movie_tag_frequency[movie_id]:
                    user_movie_tags[movie_id][tag] = movie_tag_frequency[movie_id][tag]
                else:
                    user_movie_tags[movie_id][tag] = 0

    return user_movie_tags

def generate_models(user_tags):
    models = {}

    for movie_id in user_tags:
        models[movie_id] = generate_model(user_tags[movie_id])

    return models


def generate_model(tag_dict):
    corpus = generate_corpus(tag_dict)

    lda = gensim.models.LdaModel(corpus, num_topics=5)

    return (lda, lda[corpus])

def generate_corpus(tag_dict):
    corpus = []
    document = []
    corpus.append(document)

    for tag, count in tag_dict.items():
        document.append((int(tag), count))

    return corpus

def find_similarities(models,
                      user_movies,
                      movie_tag_frequency):
    similarities = {}

    total_lhs = len(models)

    for i, lhs_movie in enumerate(models):
        model = models[lhs_movie][0]
        lhs_topic_vec = convert_to_vector(models[lhs_movie][1])
        for rhs_movie in movie_tag_frequency:
            if (rhs_movie not in user_movies and
                len(movie_tag_frequency[rhs_movie]) > 0):
                movie_tags = movie_tag_frequency[rhs_movie]
                rhs_topics = model[generate_corpus(movie_tags)]
                rhs_topic_vec = convert_to_vector(rhs_topics)

                if lhs_movie not in similarities:
                    similarities[lhs_movie] = []

                dot_product = dot(lhs_topic_vec, rhs_topic_vec)
                similarities[lhs_movie].append((rhs_movie, dot_product))
        utils.print_status(i, total_lhs, 'similarities')

    utils.print_status(total_lhs, total_lhs, 'similarities')
    print()

    return similarities

def dot(v1, v2):
    if len(v2) < len(v1):
        v2 += [0.0] * (len(v1) - len(v2))
    elif len(v1) < len(v2):
        v1 += [0.0] * (len(v2) - len(v1))
    return numpy.dot(v1, v2)

def convert_to_vector(topic_distribution):
    v = []

    for n in topic_distribution[0]:
        v.append(n[1])

    return v
