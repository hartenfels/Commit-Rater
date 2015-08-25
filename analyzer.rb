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
Dir.glob('samples/*.json') do |sample|
  authors = JSON.parse(File.read(sample))
  authors.each do |authorEmail, resultTriples|
    result[authorEmail] = Hash.new
    result[authorEmail]['passRates'] = getAuthorPassRates(resultTriples)
    result[authorEmail]['passRateWeights'] = getAuthorPassRateWeights(resultTriples)
    result[authorEmail]['average'] = result[authorEmail]['passRates'].values.inject(:+) \
                                   / result[authorEmail]['passRates'].length
    result[authorEmail]['weightedAverage'] = result[authorEmail]['passRates'].values.zip(result[authorEmail]['passRateWeights'].values).map{|a,b| a * b}.inject(:+)\
                                           / result[authorEmail]['passRateWeights'].values.inject(:+)
  end
end

puts JSON.pretty_generate(result)
