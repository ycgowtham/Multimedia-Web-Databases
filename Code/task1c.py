import numpy, utils, gensim

class T1CGlobalVariables:
    valid_actors = None

def find_similar_actors_of_an_actor(actor_id,
                                    tfidf_tags_vector,
                                    actor_tags,
                                    mode,
                                    debug=False):

    if actor_id not in tfidf_tags_vector:
        print('invalid actor id')
        return

    matrix, actorRow = vectors_to_matrix(actor_id, tfidf_tags_vector, debug)

    latent_semantics = None

    if mode == 'SVD':
        latent_semantics = compute_latent_semantics(actorRow,
                                                    get_valid_actors(tfidf_tags_vector),
                                                    *svd(matrix, debug))
    elif mode == 'PCA':
        latent_semantics = compute_latent_semantics(actorRow,
                                                    get_valid_actors(tfidf_tags_vector),
                                                    *svd(covariance(matrix), debug))
    elif mode == 'LDA':
        _model, topic_terms = generate_model(actor_tags[actor_id])
        latent_semantics = get_top_10_using_LDA_model(actor_id, _model, topic_terms, actor_tags)

    elif mode == 'TF-IDF':
        latent_semantics = compute_dot_product(actor_id, tfidf_tags_vector)

    else:
        print('unsupported mode')
        return {}

    if debug:
        print("\n**** ANGLES OF ACTORS CLOSE TO {0} ****".format(actor_id))
        print(dot_product)

        print("\n**** LATENT SEMANTICS OF ACTORS CLOSE TO {0} ****".format(actor_id))
        print(latent_semantics)

    print_output(latent_semantics)
    return latent_semantics

def get_valid_actors(tfidf_tags_vector):
    if T1CGlobalVariables.valid_actors == None:
        T1CGlobalVariables.valid_actors = set([actor for actor in tfidf_tags_vector.keys() if len(tfidf_tags_vector[actor]) > 0])
    return T1CGlobalVariables.valid_actors

def vectors_to_matrix(actorid, tfidf_tags_vector, debug):
    tag_set = set()
    actor_set = set()

    for actor in get_valid_actors(tfidf_tags_vector):
        actor_set.add(actor)
        for tag in tfidf_tags_vector[actor].keys():
            tag_set.add(tag)

    matrix = numpy.zeros((len(actor_set), len(tag_set)))

    actorRow = -1
    for row, actor in enumerate(get_valid_actors(tfidf_tags_vector)):
        if actor == actorid:
            actorRow = row
        for column, tag in enumerate(tag_set):
            if tag in tfidf_tags_vector[actor].keys():
                matrix[row][column] = tfidf_tags_vector[actor][tag]
    if debug:
        print("\n**** INPUT MATRIX ****")
        print(matrix)
    return matrix, actorRow

def compute_dot_product(actorid, tfidf_tags_vector):
    dot_products = {}
    for actor in get_valid_actors(tfidf_tags_vector):
        if actor == actorid:
            continue
        dot_products[actor] = dot_and_cosine(tfidf_tags_vector[actorid],
                                             tfidf_tags_vector[actor])
    return dot_products

def compute_latent_semantics(actorRow, actors, u, s, v):
    latent_semantics = {}
    for i, actor in enumerate(actors):
        if i != actorRow:
            latent_semantics[actor] = numpy.dot(u[actorRow][:5], u[i][:5])
    return latent_semantics

def generate_model(tag_dict):
    corpus = generate_corpus(tag_dict)

    lda = gensim.models.LdaModel(corpus, num_topics=5)

    return lda, lda[corpus]

def get_top_10_using_LDA_model(actor_id, model, topic_terms, actor_tags):
    topic_similarities = {}
    for actor in actor_tags:
        if actor != actor_id:
            topic_similarities[actor] = model[generate_corpus(actor_tags[actor])]

    chosen_actor_topics = convert_to_vector(topic_terms[0])
    dot_products = {}
    for actor in topic_similarities:
        try:
            v2 = convert_to_vector(topic_similarities[actor][0])
            if len(v2) == 5:
                dot_products[actor] = numpy.dot(v2, chosen_actor_topics)
        except:
            pass

    return dot_products

def convert_to_vector(topics):
    v = []
    for topic in topics:
        v.append(topic[1])
    return v

def generate_corpus(tag_dict):
    corpus = []
    document = []
    corpus.append(document)

    for tag, count in tag_dict.items():
        document.append((int(tag), count))

    return corpus

def dot_and_cosine(v1, v2):
    return utils.dot_product(v1, v2)[0]

def covariance(m):
    return numpy.cov(m)

def svd(m, debug):
    u, s, v = numpy.linalg.svd(m, full_matrices=False)
    if debug:
        print("\n**** u-matrix ****")
        print(u)
        print("\n**** diagonal values ****")
        print(s)
        print("\n**** v-matrix ****")
        print(v)
    return u, s, v

def print_output(latent_semantics):
    for semantic in sorted(latent_semantics.items(), key=lambda x: x[1], reverse=True)[:10]:
        print("actor_id: {0} latent-semantic-dot-product: {1}".format(*semantic))
