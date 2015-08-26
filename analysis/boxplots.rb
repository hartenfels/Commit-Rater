#!/usr/bin/ruby

require 'json'
require 'pp'
require 'statsample'

def median(array)
  sorted = array.sort
  len = sorted.length
  return (sorted[(len - 1) / 2] + sorted[len / 2]) / 2.0
end

criteria = %w[body_limit body_used capitalize_subject empty_second_line imperative_subject no_bulk_change no_duplicate no_long_message no_misspelling no_period_subject no_short_message no_vulgarity subject_limit]

repos = Hash.new
Dir.glob('../samples/*.json') do |sample|
  repos[File.basename(sample, ".json")] = JSON.parse(File.read(sample))
end

boxcount = 1
criteria.each do |criterion|
  puts "----#{criterion}"
  File.open("boxplots/#{criterion}.svg", 'w') do |f|
    boxplot_arrays = Array.new
    repos.each do |repo, authors|
      repo_criterion_rates = Array.new
      authors.each do |author, triples|
        next if triples['subject_limit']['pass'] + triples['subject_limit']['fail'] < 10
        triple = triples[criterion]
        total = triple['pass'] + triple['fail']
        rate = total == 0 ? 1 : triple['pass'].to_f / total
        repo_criterion_rates.push(rate)
      end
      puts repo
      puts median(repo_criterion_rates)
      #puts repo_criterion_rates.inject{|sum, el| sum + el}.to_f / repo_criterion_rates.size
      boxplot_arrays.push(repo_criterion_rates.to_numeric)
    end
    svg = Statsample::Graph::Boxplot.new(
      :vectors => boxplot_arrays,
      :width => 1024,
      :height => 768,
      :groups => (0..boxplot_arrays.length).to_a
    ).to_svg
    repos.length.times do
      repo_index = boxcount % repos.length - 1
      svg.sub! ">Vector #{boxcount}<", ">#{repos.keys[repo_index]} (#{repos.values[repo_index].length})<"
      boxcount += 1
    end
    f.puts svg
  end
end
