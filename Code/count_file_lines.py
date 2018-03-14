import csv, json

files = [
    'genome-tags.csv',
    'imdb-actor-info.csv',
    'mlmovies.csv',
    'mlratings.csv',
    'mltags.csv',
    'mlusers.csv',
    'movie-actor.csv'
]

def main():
    lengths = {}
    with open('file_lengths.json', 'w') as file_lengths:
        for f in files:
            with open('../data/' + f) as csvFile:
                reader = csv.reader(csvFile)
                row_count = sum(1 for row in csvFile)
                lengths[f] = row_count
        json.dump(lengths, file_lengths)

if __name__ == '__main__':
    main()
