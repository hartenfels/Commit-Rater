#!/usr/bin/ruby

require 'json'
require 'pp'

criteria = %w[body_limit body_used capitalize_subject empty_second_line imperative_subject no_bulk_change no_duplicate no_long_message no_misspelling no_period_subject no_short_message no_vulgarity subject_limit]

repos = Hash.new

Dir.glob('samples/*.json') do |sample|
  repos[sample] = JSON.parse(File.read(sample))
end

criteria.each do |criterion|
  File.open("boxplots/#{criterion}.txt", 'w') {|f|
    repos.each do |repo|
      repo.each do |author|
        
      end
    end
    #f.puts existing
  }
end
