#!/usr/bin/ruby

require 'json'
require 'pp'

def getAuthorPassRates(resultTriples)
  passRates = Hash.new
  resultTriples.each do |criterionName, resultTriple|
    passRates[criterionName] = resultTriple['pass'].to_f \
                             / (resultTriple['pass'] + resultTriple['fail'])
  end
  return passRates
end

def getAuthorPassRateWeights(resultTriples)
  passRateWeights = Hash.new
  resultTriples.each do |criterionName, resultTriple|
    passRateWeights[criterionName] = (resultTriple['pass'].to_f + resultTriple['fail']) \
                                   / (resultTriple['pass'] + resultTriple['fail'] + resultTriple['undef'])
  end
  return passRateWeights
end

def countCommits(resultTriples)
  return resultTriples['subject_limit']['pass'] + resultTriples['subject_limit']['fail']
end

criteria = [
  "body_limit",
  "body_used",
  "capitalize_subject",
  "empty_second_line",
  "imperative_subject",
  "no_bulk_change",
  "no_duplicate",
  "no_long_message",
  "no_misspelling",
  "no_period_subject",
  "no_short_message",
  "no_vulgarity",
  "subject_limit"
]

result = Hash.new
result['authors'] = Hash.new
Dir.glob('samples/*.json') do |sample|
  authors = JSON.parse(File.read(sample))
  authors.each do |authorEmail, resultTriples|
    next if countCommits(resultTriples) < 10

    result['authors'][authorEmail] = Hash.new

    passRates = getAuthorPassRates(resultTriples)
    result['authors'][authorEmail]['passRates'] = passRates

    passRateWeights = getAuthorPassRateWeights(resultTriples)
    result['authors'][authorEmail]['passRateWeights'] = passRateWeights

    average = passRates.values.inject(:+) / passRates.length
    result['authors'][authorEmail]['average'] = average

    weightedAverage = passRates.values.zip(passRateWeights.values).map{|a,b| a * b}.inject(:+)\
                    / passRateWeights.values.inject(:+)
    result['authors'][authorEmail]['weightedAverage'] = weightedAverage
  end
end

result['averageRating'] = result['authors'].values.inject(0){|acc, a| acc + a['average']} / result['authors'].length
result['averageWeightedRating'] = result['authors'].values.inject(0){|acc, a| acc + a['weightedAverage']} / result['authors'].length
criteria.each do |criterion|

  criterionAverage = result['authors'].values.inject(0){|acc, a| acc + a['passRates'][criterion]} \
                   / result['authors'].length
  result["average_#{criterion}"] = criterionAverage
end

puts JSON.pretty_generate(result)
