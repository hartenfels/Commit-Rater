#!/usr/bin/ruby

require 'json'
require 'pp'
require 'statsample'

# ss_analysis(Statsample::Graph::Boxplot) do
#   a = [0.0, 0.1, 0.3, 0.5, 0.7, 0.99].to_numeric
#   b = [0.0, 0.1, 0.125, 0.15, 0.1, 0.2, 0.99, 0.05].to_numeric
#   c = [0.5, 0.51, 0.52, 0.53, 0.54].to_numeric
#   d = [1, 1].to_numeric
#
#   boxplot(:vectors=>[d], :width=>600, :height=>600, :groups=>[0,1,2])
# end
# Statsample::Analysis.run


criteria = %w[body_limit body_used capitalize_subject empty_second_line imperative_subject no_bulk_change no_duplicate no_long_message no_misspelling no_period_subject no_short_message no_vulgarity subject_limit]

repos = Hash.new
Dir.glob('../samples/*.json') do |sample|
  repos[File.basename(sample, ".json")] = JSON.parse(File.read(sample))
end

boxcount = 1
criteria.each do |criterion|
  puts "#{criterion}"
  File.open("boxplots/#{criterion}.svg", 'w') do |f|
    boxplot_arrays = Array.new
    repos.each do |repo, authors|
      repo_criterion_rates = Array.new
      authors.each do |author, triples|
        triple = triples[criterion]
        total = triple['pass'] + triple['fail']
        rate = total == 0 ? 1 : triple['pass'].to_f / total
        repo_criterion_rates.push(rate)
      end
      boxplot_arrays.push(repo_criterion_rates.to_numeric)
    end
    svg = Statsample::Graph::Boxplot.new(
      :vectors => boxplot_arrays,
      :width => 1024,
      :height => 768,
      :groups => (0..boxplot_arrays.length).to_a
    ).to_svg
    repos.length.times do
      svg.sub! ">Vector #{boxcount}<", ">#{repos.keys[boxcount % repos.length]}<"
      boxcount += 1
    end
    f.puts svg
  end
end
