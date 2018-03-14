import re, sys

from datetime import datetime

# Singleton class
class WeightConstants:
    timestamp_max = 0
    timestamp_min = 9999999999999
    movie_name_map = {}
    movie_metadata_map = {}
    user_movies = {}
    init_exception = Exception('WeightConstants is not initialized!')
    feedbackFormat = re.compile(r'(\s*,*(\s*[1-9]+\s*,+\s*)*[1-9]+\s*,*\s*)|(\s*q+\s*)')
    feedbackParse = re.compile(r'(\d+|q)')

    @staticmethod
    def reset():
        WeightConstants.timestamp_max = 0
        WeightConstants.timestamp_min = 9999999999999
        WeightConstants.movie_name_map = {}
        WeightConstants.movie_metadata_map = {}
        WeightConstants.user_movies = {}

    @staticmethod
    def is_initialized():
        return (WeightConstants.timestamp_max != 0 and
                WeightConstants.timestamp_min != 9999999999999 and
                len(WeightConstants.movie_name_map) != 0)

    @staticmethod
    def initialize(movie_map,
                   movie_tag,
                   movie_rating):
        if WeightConstants.is_initialized():
            return
        elif (movie_map == None or
              movie_tag == None or
              movie_rating == None):
            return

        WeightConstants.movie_name_map = movie_map

        for row in movie_tag:
            timestamp = convert_timestamp(row['timestamp'])

            WeightConstants.update_timestamp_constants(timestamp)

            WeightConstants.update_movie_metadata(row['movieid'],
                                                  row['userid'],
                                                  timestamp=row['timestamp'])
        for row in movie_rating:
            timestamp = convert_timestamp(row['timestamp'])

            WeightConstants.update_timestamp_constants(timestamp)

            WeightConstants.update_movie_metadata(row['movieid'],
                                                  row['userid'],
                                                  timestamp=row['timestamp'])

    @staticmethod
    def update_timestamp_constants(timestamp):
        WeightConstants.timestamp_max = WeightConstants.max_timestamp(timestamp)
        WeightConstants.timestamp_min = WeightConstants.min_timestamp(timestamp)

    @staticmethod
    def max_timestamp(timestamp):
        return max(timestamp, WeightConstants.timestamp_max)

    @staticmethod
    def min_timestamp(timestamp):
        return min(timestamp, WeightConstants.timestamp_min)

    @staticmethod
    def update_movie_metadata(movie_id,
                              user_id,
                              timestamp):
        if not movie_id in WeightConstants.movie_metadata_map:
            WeightConstants.movie_metadata_map[movie_id] = {
                'timestamp': {},
            }

        movie_metadata = WeightConstants.movie_metadata_map[movie_id]

        if timestamp != None and user_id != None:
            movie_timestamps = movie_metadata['timestamp']

            if not user_id in WeightConstants.user_movies:
                WeightConstants.user_movies[user_id] = set()
            WeightConstants.user_movies[user_id].add(movie_id)

            if user_id in movie_timestamps:
                movie_timestamps[user_id] = max(convert_timestamp(timestamp),
                                                movie_timestamps[user_id])
            else:
                movie_timestamps[user_id] = convert_timestamp(timestamp)

    @staticmethod
    def get_movie_timestamp(user_id, movie_id):
        if not WeightConstants.is_initialized():
            raise WeightConstants.init_exception
        movie_metadata = WeightConstants.movie_metadata_map[movie_id]
        return movie_metadata['timestamp'][user_id]

    @staticmethod
    def get_movie_name(movie_id):
        if not WeightConstants.is_initialized():
            raise WeightConstants.init_exception
        return WeightConstants.movie_name_map[movie_id]

    @staticmethod
    def get_user_movies(user_id):
        if not WeightConstants.is_initialized():
            raise WeightConstants.init_exception
        return WeightConstants.user_movies[user_id]

def weigh_similarities(user_id,
                       similarities,
                       movie_year=None,
                       movie_tag=None,
                       movie_rating=None):

    WeightConstants.initialize(movie_year,
                               movie_tag,
                               movie_rating)

    weight_map = {}

    for lhs_movie in similarities:
        movie_sim = (similarities[lhs_movie]
                        if isinstance(similarities[lhs_movie], list)
                        else similarities[lhs_movie].items())
        for rhs_movie, similarity in movie_sim:
            if not rhs_movie in weight_map:
                weight_map[rhs_movie] = get_weight(user_id,
                                                    lhs_movie,
                                                    rhs_movie,
                                                    similarity)
            else:
                weight_map[rhs_movie] += get_weight(user_id,
                                                    lhs_movie,
                                                    rhs_movie,
                                                    similarity)
    weight_list = []
    for movie_id in weight_map:
        weight_list.append((movie_id, weight_map[movie_id]))

    return sorted(weight_list, key=lambda x: x[1], reverse=True)

def get_weight(user_id, lhs_movie, rhs_movie, similarity):
    movie_timestamp = WeightConstants.get_movie_timestamp(user_id, lhs_movie)

    return compute_weight(similarity, movie_timestamp)

def compute_weight(similarity, timestamp):
    if not WeightConstants.is_initialized():
        raise WeightConstants.init_exception

    t_max = WeightConstants.timestamp_max
    t_min = WeightConstants.timestamp_min

    weight = (similarity *
                compute_time_weight(timestamp, t_min, t_max))

    return weight

def compute_time_weight(t, t_min, t_max):
    weight = (t - t_min + 1) / (t_max - t_min + 1)
    return weight

def convert_timestamp(t):
    if is_epoch(t):
        return t
    else:
        return datetime.strptime(t, '%Y-%m-%d %H:%M:%S').timestamp()

def is_epoch(t):
    try:
        x = int(t)
        return True
    except:
        return False

def print_output_using(user_id, similarities, feedbackFunction=None, x=5):
    if feedbackFunction == None:
        print_output(similarities, x)
    else:
        weights = similarities
        rel_movies = WeightConstants.get_user_movies(user_id)
        irrel_movies = set()
        while True:
            rel_indices = get_indices_from_movies(weights, rel_movies)
            irrel_indices = get_indices_from_movies(weights, irrel_movies)

            print_output_enumerated(weights, x, rel_indices, irrel_indices)

            indices = None
            while indices == None:
                indices = get_input_for_feedback(x)

            if indices['rel'] == set() and indices['irrel'] == set():
                break

            rel, irrel = get_rel_and_irrel_movies(indices, weights)

            rel_movies = (rel_movies - irrel) | rel
            irrel_movies = (irrel_movies - rel) | irrel

            rel_feedback = feedbackFunction(rel_movies)
            irrel_feedback = feedbackFunction(irrel_movies)

            feedback = aggregate_relevant_and_irrelevant({},
                                                         rel_feedback,
                                                         irrel_feedback)

            weights = create_feedback_weight(similarities, feedback)

def print_output(similarities, x=None):
    sim = similarities[:x] if x != None and x > 0 else similarities
    for pair in sim:
        print_pair(pair)

def print_output_enumerated(similarities, x=None, rel=set(), irrel=set()):
    sim = similarities[:x] if x != None and x > 0 else similarities
    for i, pair in enumerate(sim):
        print("[{0}]: ".format(i + 1), end='', flush=True)

        if i in rel:
            print_pair(pair, True)
        elif i in irrel:
            print_pair(pair, False)
        else:
            print_pair(pair)

def print_pair(similarity_pair, rel=None):
    movie_id = similarity_pair[0]
    movie_name = WeightConstants.get_movie_name(similarity_pair[0])
    movie_weight = similarity_pair[1]

    print("{0} - {1} - weight {2}".format(movie_id,
                                          movie_name,
                                          movie_weight), end='')
    if rel == True:
        print(" (relevant)")
    elif rel == False:
        print(" (irrelevant)")
    else:
        print()

def get_input_for_feedback(x):
    print("\nPlease input relevant entries," +
            "separated by commas (e.g. 1,2,3) or 'q' to end:")
    rel_input_str = input("> ")


    if is_feedback_string_valid(rel_input_str):
        rel_indices = parse_input_string(rel_input_str)

        if not validate_indices(rel_indices, x):
            return None

        print("\nPlease input irrelevant entries," +
                "separated by commas (e.g. 1,2,3) or 'q' to end:")
        irrel_input_str = input("> ")

        if is_feedback_string_valid(irrel_input_str):
            irrel_indices = parse_input_string(irrel_input_str)

            if len(irrel_indices.intersection(rel_indices)) > 0:
                return None

            if not validate_indices(irrel_indices, x):
                return None

            return {
                'rel': rel_indices,
                'irrel': irrel_indices
            }
        else:
            return None
    else:
        return None

def is_feedback_string_valid(inputString):
    match = WeightConstants.feedbackFormat.fullmatch(inputString)
    if match != None:
        return True
    else:
        return False

def parse_input_string(inputString):
    matches = WeightConstants.feedbackParse.findall(inputString)
    indices = set()
    if 'q' not in matches:
        for match in matches:
            indices.add(int(match) - 1)
    return indices

def get_rel_and_irrel_movies(indices, similarities):
    relevantMovies = get_movies_from_indices(similarities, indices['rel'])
    irrelevantMovies = get_movies_from_indices(similarities, indices['irrel'])

    return relevantMovies, irrelevantMovies

def aggregate_relevant_and_irrelevant(feedback, rel, irrel):
    feedback = aggregate_feedback(feedback, rel)
    feedback = aggregate_feedback(feedback, irrel, irrel=True)
    return feedback

def aggregate_feedback(prev, new, irrel=False):
    for lhs_movie in new:
        for rhs_movie in new[lhs_movie]:
            if rhs_movie in prev:
                if not irrel:
                    prev[rhs_movie] += new[lhs_movie][rhs_movie]
                else:
                    prev[rhs_movie] -= new[lhs_movie][rhs_movie]
            else:
                if not irrel:
                    prev[rhs_movie] = new[lhs_movie][rhs_movie]
                else:
                    prev[rhs_movie] = -1.0 * new[lhs_movie][rhs_movie]
    return prev

def create_feedback_weight(similarities, feedback):
    weights = []
    for movie, weight in similarities:
        newPair = None
        if movie in feedback:
            mov_fb = feedback[movie]
            if mov_fb >= 0:
                newPair = (movie, weight * max(mov_fb, sys.float_info.min))
            elif mov_fb < 0:
                newPair = (movie, weight * min(mov_fb, -1.0 * sys.float_info.min))
        else:
            newPair = (movie, weight)
        weights.append(newPair)
    return sorted(weights, key=lambda x: x[1], reverse=True)

def validate_indices(indices, x):
    for i in indices:
        if i > x:
            return False
    return True

def get_movies_from_indices(weights, indices):
    return frozenset([weights[i][0] for i in indices])

def get_indices_from_movies(weights, movies):
    indices = set()
    if len(movies) > 0:
        for i, pair in enumerate(weights):
            if pair[0] in movies:
                indices.add(i)
    return indices
