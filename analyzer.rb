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

result = Hash.new
result['authors'] = Hash.new
Dir.glob('samples/*.json') do |sample|
  authors = JSON.parse(File.read(sample))
  authors.each do |authorEmail, resultTriples|
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

puts JSON.pretty_generate(result)
