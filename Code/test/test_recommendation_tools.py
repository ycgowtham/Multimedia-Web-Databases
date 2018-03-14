import sys, unittest

from contextlib import contextmanager
from unittest.mock import patch

sys.path.append('..')
sys.path.append('.')

import recommendation_tools as rt

class Recommendation_Tools_Unit_Tests(unittest.TestCase):
    def tearDown(self):
        rt.WeightConstants.reset()

    def assert_init_exception(self, e):
        self.assertEqual('WeightConstants is not initialized!', str(e))

    def test_initialize(self):
        self.assertFalse(rt.WeightConstants.is_initialized())

        try:
            rt.WeightConstants.get_movie_timestamp('1', 'M1')
            self.fail("Expected exception thrown!")
        except Exception as e:
            self.assert_init_exception(e)

        try:
            rt.WeightConstants.get_movie_name('M1')
            self.fail("Expected exception thrown!")
        except Exception as e:
            self.assert_init_exception(e)

        try:
            rt.WeightConstants.get_user_movies('1')
            self.fail("Expected exception thrown!")
        except Exception as e:
            self.assert_init_exception(e)

        try:
            rt.compute_weight(1.00, 0.123)
            self.fail("Expected exception thrown!")
        except Exception as e:
            self.assert_init_exception(e)

        movie_map = {
            'M1': 'Movie 1 Name',
        }

        movie_tag = [{
            'userid': '1',
            'movieid': 'M1',
            'tagid': 'Tag1',
            'timestamp': "2006-11-30 04:19:50"
        }]

        movie_rating = [{
            'movieid': 'M1',
            'userid': '1',
            'imdbid': '1',
            'rating': '5',
            'timestamp': "2017-09-30 04:19:50"
        }]

        rt.WeightConstants.initialize(movie_map,
                                      movie_tag,
                                      movie_rating)

        self.assertTrue(rt.WeightConstants.is_initialized())

        try:
            rt.WeightConstants.get_movie_timestamp('1', 'M1')
        except Exception as e:
            self.fail("No exception expected!")

        try:
            rt.WeightConstants.get_movie_name('M1')
        except Exception as e:
            self.fail("No exception expected!")

        actual = None

        try:
            actual = rt.WeightConstants.get_user_movies('1')
        except Exception as e:
            self.fail("No exception expected!")

        self.assertEqual({'M1'}, actual)

        try:
            rt.compute_weight(1.00, 0.123)
        except Exception as e:
            self.fail("No exception expected!")

        rt.WeightConstants.reset()

        self.assertFalse(rt.WeightConstants.is_initialized())

    def test_weigh_similarities(self):
        userid = '1'

        similarities = {
            'Movie1' : [('Movie2', 0.324542),
                        ('Movie3', 0.123245)],
            'Movie4' : [('Movie2', 0.212345),
                        ('Movie5', 0.443213),
                        ('Movie6', 0.556673)]
        }

        movie_map = {
            'Movie1': 'Movie 1 Name',
            'Movie2': 'Movie 2 Name',
            'Movie3': 'Movie 3 Name',
            'Movie4': 'Movie 4 Name',
            'Movie5': 'Movie 5 Name',
            'Movie6': 'Movie 6 Name',
            'Movie7': 'Movie 7 Name'
        }

        movie_tag = [{
            'userid': '1',
            'movieid': 'Movie1',
            'tagid': 'Tag1',
            'timestamp': "2006-11-30 04:19:50"
        }, {
            'userid': '2',
            'movieid': 'Movie1',
            'tagid': 'Tag2',
            'timestamp': "2006-11-30 04:19:50"
        }, {
            'userid': '1',
            'movieid': 'Movie4',
            'tagid': 'Tag3',
            'timestamp': "2014-11-30 04:19:50",
        }, {
            'userid': '1',
            'movieid': 'Movie7',
            'tagid': 'Tag1',
            'timestamp': "2006-11-30 04:19:50"
        }]

        movie_rating = [{
            'movieid': 'Movie1',
            'userid': '1',
            'imdbid': '1',
            'rating': '5',
            'timestamp': "2017-09-30 04:19:50"
        }, {
            'movieid': 'Movie2',
            'userid': '1',
            'imdbid': '2',
            'rating': '4',
            'timestamp': "2013-11-30 04:19:50",
        }, {
            'movieid': 'Movie2',
            'userid': '2',
            'imdbid': '2',
            'rating': '4',
            'timestamp': "2017-01-30 04:19:50",
        },{
            'movieid': 'Movie4',
            'userid': '1',
            'imdbid': '4',
            'rating': '5',
            'timestamp': "2010-11-05 04:19:50"
        }, {
            'movieid': 'Movie7',
            'userid': '1',
            'imdbid': '7',
            'rating': '5',
            'timestamp': "2006-11-30 04:19:50"
        }]

        expected = [
            ('Movie2', 0.4813596945426980),
            ('Movie6', 0.4111054014653870),
            ('Movie5', 0.3273147041435070),
            ('Movie3', 0.1232450000000000)
        ]

        actual = rt.weigh_similarities(userid,
                                        similarities,
                                        movie_map,
                                        movie_tag,
                                        movie_rating)

        for i in range(len(expected)):
            if i > len(actual):
                self.fail("Actual is missing rows!")
            self.assertEqual(expected[i][0],
                                actual[i][0],
                                "Index {0}".format(i))
            self.assertAlmostEqual(expected[i][1],
                                    actual[i][1],
                                    msg="Index {0}".format(i),
                                    delta=0.0001)

        if len(actual) > len(expected):
            self.fail("Actual has too many rows!")

    def test_match_feedback_string(self):
        self.assertTrue(rt.is_feedback_string_valid('1,2,3'))
        self.assertTrue(rt.is_feedback_string_valid('1,2,3,4,'))
        self.assertTrue(rt.is_feedback_string_valid('4,33,,2'))
        self.assertTrue(rt.is_feedback_string_valid('4,33,2,,,'))
        self.assertTrue(rt.is_feedback_string_valid(',,,3'))
        self.assertTrue(rt.is_feedback_string_valid('1, 2, 3'))
        self.assertTrue(rt.is_feedback_string_valid('1, 2,    3'))
        self.assertTrue(rt.is_feedback_string_valid('1, 2,    3 ,  4'))
        self.assertTrue(rt.is_feedback_string_valid('   1, 2,   3'))
        self.assertTrue(rt.is_feedback_string_valid('q'))
        self.assertTrue(rt.is_feedback_string_valid('   q   '))
        self.assertTrue(rt.is_feedback_string_valid(' qq '))

        self.assertFalse(rt.is_feedback_string_valid('a,b,c'))
        self.assertFalse(rt.is_feedback_string_valid('1,a,2'))
        self.assertFalse(rt.is_feedback_string_valid('1,5,2.0'))
        self.assertFalse(rt.is_feedback_string_valid('1,4,2,,,,,,a'))
        self.assertFalse(rt.is_feedback_string_valid(',,,,,,'))
        self.assertFalse(rt.is_feedback_string_valid('1, 2,     ,b'))
        self.assertFalse(rt.is_feedback_string_valid('0,1'))
        self.assertFalse(rt.is_feedback_string_valid('-1,23'))
        self.assertFalse(rt.is_feedback_string_valid('q,1,3'))

    def test_parse_input_string(self):
        string = '1,2,3'
        expected = {0,1,2}
        actual = rt.parse_input_string(string)
        self.assertEqual(expected, actual)

        string = '12,13,14'
        expected = {11,12,13}
        actual = rt.parse_input_string(string)
        self.assertEqual(expected, actual)

        string = '     144, 2  , 45   '
        expected = {143, 1, 44}
        actual = rt.parse_input_string(string)
        self.assertEqual(expected, actual)

        string = '    ,,,,,4, 11,   2,,,,  '
        expected = {3, 10, 1}
        actual = rt.parse_input_string(string)
        self.assertEqual(expected, actual)

        string = '    q  '
        expected = set()
        actual = rt.parse_input_string(string)
        self.assertEqual(expected, actual)

        string = '   qqq  '
        expected = set()
        actual = rt.parse_input_string(string)
        self.assertEqual(expected, actual)

    def test_get_rel_and_irrel_movies(self):
        similarities = [('1', 1.0),
            ('2', 2.0),
            ('3', 6.0),
            ('4', 8.0),
            ('5', 9.0)
        ]

        mov = {
            'rel': {1, 3},
            'irrel': {0, 2, 4}
        }

        expected_rel = {'2', '4'}
        expected_irrel = {'1','3','5'}

        act_rel, act_irrel = rt.get_rel_and_irrel_movies(mov,
                                                         similarities)

        self.assertEqual(expected_rel, act_rel)
        self.assertEqual(expected_irrel, act_irrel)

        mov = {
            'rel': {0, 1, 2, 4},
            'irrel': {3}
        }

        expected_rel = {'1','2','3','5'}
        expected_irrel = {'4'}

        act_rel, act_irrel = rt.get_rel_and_irrel_movies(mov,
                                                         similarities)

        self.assertEqual(expected_rel, act_rel)
        self.assertEqual(expected_irrel, act_irrel)

        mov = {
            'rel': {0, 1, 2, 4},
            'irrel': set()
        }

        expected_rel = {'1','2','3','5'}
        expected_irrel = frozenset()

        act_rel, act_irrel = rt.get_rel_and_irrel_movies(mov,
                                                         similarities)

        self.assertEqual(expected_rel, act_rel)
        self.assertEqual(expected_irrel, act_irrel)

    def test_get_input_for_feedback(self):
        printstr_1 = ("\nPlease input relevant entries," +
                      "separated by commas (e.g. 1,2,3) or 'q' to end:")

        printstr_2 = ("\nPlease input irrelevant entries," +
                      "separated by commas (e.g. 1,2,3) or 'q' to end:")

        exp_print_strs = [[printstr_1, '\n', printstr_2, '\n']]

        def mock_input(strings):
            for s in (strings + ['q']):
                yield s

        expected = None
        with patch('builtins.input', side_effect=mock_input(['1,2,3','4'])):
            with mock_print(exp_print_strs, self):
                actual = rt.get_input_for_feedback(2)
                self.assertEqual(expected, actual)

        expected = {
            'rel': {0,1,2},
            'irrel': {3}

        }
        with patch('builtins.input', side_effect=mock_input(['1,2,3','4'])):
            with mock_print(exp_print_strs, self):
                actual = rt.get_input_for_feedback(5)
                self.assertEqual(expected, actual)

        expected = None
        with patch('builtins.input', side_effect=mock_input(['1,4,3','4'])):
            with mock_print(exp_print_strs, self):
                actual = rt.get_input_for_feedback(5)
                self.assertEqual(expected, actual)

        expected = None
        with patch('builtins.input', side_effect=mock_input(['1,2,3','3'])):
            with mock_print(exp_print_strs, self):
                actual = rt.get_input_for_feedback(5)
                self.assertEqual(expected, actual)

        expected = None
        with patch('builtins.input', side_effect=mock_input([',,,-1'])):
            with mock_print(exp_print_strs, self):
                actual = rt.get_input_for_feedback(5)
                self.assertEqual(expected, actual)

    def test_print_output_enumerated(self):
        movie_map = {
            '1': 'Movie 1 Name',
            '2': 'Movie 2 Name',
            '3': 'Movie 3 Name'
        }

        movie_tag = [{
            'userid': '1',
            'movieid': '1',
            'tagid': 'Tag1',
            'timestamp': "2006-11-30 04:19:50"
        }]

        movie_rating = [{
            'movieid': '1',
            'userid': '1',
            'imdbid': '1',
            'rating': '5',
            'timestamp': "2017-09-30 04:19:50"
        }]

        rt.WeightConstants.initialize(movie_map,
                                      movie_tag,
                                      movie_rating)

        similarities = [('1', 1.0), ('2', 2.0), ('3', 3.0)]

        exp_str = [['[1]: ' , '', '1 - Movie 1 Name - weight 1.0', '', '\n',
            '[2]: ', '', '2 - Movie 2 Name - weight 2.0', '', '\n',
            '[3]: ', '', '3 - Movie 3 Name - weight 3.0', '', '\n'
        ]]

        with mock_print(exp_str, self):
            rt.print_output_enumerated(similarities)

        exp_str = [['[1]: ' , '', '1 - Movie 1 Name - weight 1.0', '', '\n',
            '[2]: ', '', '2 - Movie 2 Name - weight 2.0', '', '\n'
        ]]

        with mock_print(exp_str, self):
            rt.print_output_enumerated(similarities, 2)

        exp_str = [['[1]: ' , '', '1 - Movie 1 Name - weight 1.0', '', ' (relevant)', '\n',
            '[2]: ', '', '2 - Movie 2 Name - weight 2.0', '', '\n',
            '[3]: ', '', '3 - Movie 3 Name - weight 3.0', '', ' (irrelevant)', '\n'
        ]]

        with mock_print(exp_str, self):
            rt.print_output_enumerated(similarities, 5, {0}, {2})

    def test_print_output_using(self):
        movie_map = {
            '1': 'Movie 1 Name',
            '2': 'Movie 2 Name',
            '3': 'Movie 3 Name',
            '4': 'Movie 4 Name'
        }

        movie_tag = [{
            'userid': '1',
            'movieid': '5',
            'tagid': 'Tag1',
            'timestamp': "2006-11-30 04:19:50"
        }]

        movie_rating = [{
            'movieid': '5',
            'userid': '1',
            'imdbid': '1',
            'rating': '5',
            'timestamp': "2017-09-30 04:19:50"
        }]

        rt.WeightConstants.initialize(movie_map,
                                      movie_tag,
                                      movie_rating)

        similarities = [('3', 3.0), ('2', 2.0), ('1', 1.0), ('4', 0.5)]

        exp_str = ['[1]: ' , '', '3 - Movie 3 Name - weight 3.0', '', '\n',
            '[2]: ', '', '2 - Movie 2 Name - weight 2.0', '', '\n',
            '[3]: ', '', '1 - Movie 1 Name - weight 1.0', '', '\n',
            '[4]: ', '', '4 - Movie 4 Name - weight 0.5', '', '\n'
        ]

        printstr_1 = ("\nPlease input relevant entries," +
                        "separated by commas (e.g. 1,2,3) or 'q' to end:")


        printstr_2 = ("\nPlease input irrelevant entries," +
                        "separated by commas (e.g. 1,2,3) or 'q' to end:")

        exp_str_3 = [
            '[1]: ', '', '3 - Movie 3 Name - weight 9.0', '', ' (relevant)','\n',
            '[2]: ', '', '2 - Movie 2 Name - weight 4.0', '', ' (irrelevant)', '\n',
            '[3]: ', '', '4 - Movie 4 Name - weight 2.5', '', ' (relevant)','\n',
            '[4]: ', '', '1 - Movie 1 Name - weight 2.0', '', ' (irrelevant)', '\n'
        ]

        exp_str_4 = [
            '[1]: ', '', '3 - Movie 3 Name - weight 9.0', '', ' (relevant)', '\n',
            '[2]: ', '', '2 - Movie 2 Name - weight 4.0', '', ' (irrelevant)', '\n',
            '[3]: ', '', '4 - Movie 4 Name - weight 2.5', '', ' (relevant)', '\n',
            '[4]: ', '', '1 - Movie 1 Name - weight 2.0', '', ' (irrelevant)', '\n'
        ]

        exp_strs = [
            exp_str,
            [printstr_1, '\n'],
            [printstr_1, '\n'],
            [printstr_2, '\n'],
            exp_str_3,
            [printstr_1, '\n'],
            [printstr_2, '\n'],
            exp_str_4,
            [printstr_1, '\n'],
            [printstr_2, '\n'],
        ]

        def mock_input():
            return_vals = [',,,-1', '1,4', '2,3', '1,3', '2,4', 'q', 'q']
            for return_val in return_vals:
                    yield return_val

        class OutputCheck:
            was_called = False
            switch = True

            @staticmethod
            def flip():
                OutputCheck.switch = (not OutputCheck.switch)

            @staticmethod
            def check_output(output):
                OutputCheck.was_called = True
                if OutputCheck.switch:
                    exp_rel = {'3', '4', '5'}
                    self.assertEqual(exp_rel, output)
                    OutputCheck.flip()
                    return {
                            '1': {
                                '1': 1.0,
                                '2': 2.0,
                                '3': 3.0
                            },
                    }
                else:
                    exp_irrel = {'1','2'}
                    self.assertEqual(exp_irrel, output)
                    OutputCheck.flip()
                    return {
                        '2': {
                            '1': -1.0,
                            '2': -2.0,
                            '4': -5.0
                        },
                        '3': {
                            '2': 2.0
                        }
                    }

        with mock_print(exp_strs, self):
            with patch('builtins.input', side_effect=mock_input()) as mock:
                actual = rt.print_output_using('1',
                                               similarities,
                                               OutputCheck.check_output)
            StdOutMock.all_checked()
        self.assertTrue(OutputCheck.was_called)

@contextmanager
def mock_print(expected_strs, unittest_instance, num_times=1):
    StdOutMock.set__(expected_strs, unittest_instance, num_times)
    _oldstdout = sys.stdout
    sys.stdout = StdOutMock
    yield
    sys.stdout = _oldstdout

class StdOutMock:
    expected = ''
    unittest_inst = None
    index = 0
    num = 0
    num_times = 1
    can_check = True

    @staticmethod
    def write(actual):
        i = StdOutMock.index
        StdOutMock.unittest.assertEqual(StdOutMock.expected[StdOutMock.num][i],
                                        actual)
        StdOutMock.index += 1
        if StdOutMock.index == len(StdOutMock.expected[StdOutMock.num]):
            StdOutMock.num += 1
            StdOutMock.all_checked()
            StdOutMock.index = 0

    @staticmethod
    def flush():
        pass

    @staticmethod
    def all_checked():
        if StdOutMock.num == len(StdOutMock.expected):
            return
        i = StdOutMock.index
        l = len(StdOutMock.expected[StdOutMock.num - 1])
        StdOutMock.unittest.assertEqual(i, l)

    @staticmethod
    def set__(expected_strs, unittest_inst, num_times):
        StdOutMock.expected = expected_strs
        StdOutMock.unittest = unittest_inst
        StdOutMock.num_times = num_times
        StdOutMock.num = 0
        StdOutMock.index = 0

if __name__ == '__main__':
    unittest.main()
