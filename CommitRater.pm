package CommitRater;
use Moose;
use feature        qw(fc state);
use List::Util     qw(all any);
use Lingua::EN::Tagger;
use Regexp::Common qw(profanity);
use Text::Aspell;
use CommitRater::Repo;


has repo  => (
    is       => 'ro',
    isa      => 'Maybe[CommitRater::Repo]',
    required => 1,
);

has dups  => (
    is      => 'ro',
    isa     => 'HashRef',
    default => sub { {} },
);

has spell => (
    is      => 'ro',
    default => sub
    {
        my $spell = Text::Aspell->new;
        $spell->set_option(lang => 'en_US');
        return $spell
    },
);


use constant RULES => qw(
    empty_second_line
    subject_limit
    capitalize_subject
    no_period_subject
    imperative_subject
    body_limit
    body_used
);

use constant NONOS => qw(
    no_short_message
    no_long_message
    no_bulk_change
    no_vulgarity
    no_misspelling
    no_duplicate
);

use constant ALL_KEYS => (RULES, NONOS);


sub rate_base
{
    my ($self, $callback, $accu, @rest) = @_;
    $self->repo->update;
    $self->repo->each_commit(sub { $callback->($self, $_, $accu) }, @rest);
    return $accu
}

sub rate
{
    my ($self, $limit) = @_;
    return $self->rate_base(\&rate_commit, {}, $limit)
}

sub rate_independent
{
    my ($self, $limit) = @_;
    return $self->rate_base(\&rate_commit_independent, [], $limit)
}


sub default_result { map { $_ => {pass => 0, fail => 0, undef => 0} } ALL_KEYS }

sub rate_commit
{
    my ($self, $commit, $results) = @_;

    my $author = $results->{fc $commit->{email}} //= {default_result};
    my $rules  = $self->rate_message($commit);

    while (my ($k, $v) = each %$rules)
    {
        my $what = $v ? 'pass' : defined $v ? 'fail' : 'undef';
        $author->{$k}{$what} += 1;
    }
}

sub rate_commit_independent
{
    my ($self, $commit, $results) = @_;
    my $rules = $self->rate_message($commit);
    push @{$results}, [@{$rules}{(ALL_KEYS)}];
}


sub rate_message
{
    no warnings 'uninitialized';
    state $tagger = Lingua::EN::Tagger->new;
    my ($self, $commit)  = @_;
    my ($subject, @body) = @{$commit->{message}};
    my $subject_words    = split ' ', $subject;
    my $text             = join "\n", $subject, @body;
    my @words            = grep { /\w+/ } split ' ', $text;

    my %result;
    @result{(ALL_KEYS)} = (
        undef,                                           # empty_second_line
        length $subject <= 50,                           # subject_limit
        scalar($subject =~ /^\p{Uppercase}/),            # capitalize_subject
        scalar($subject !~ /\.\s*$/),                    # no_period_subject
        scalar($tagger->add_tags($subject) =~ /<vbp?>/), # imperative subject
        undef,                                           # body_limit
        scalar(any { /\S/ } @body),                      # body used

        $subject_words >  2,                             # no_short_message
        $subject_words < 10,                             # no_long_message
        @{$commit->{changes}} < 10,                      # no_bulk_change
        $text !~ /$RE{profanity}{contextual}/,           # no_vulgarity
        scalar(grep { !$self->spell->check($_) } @words) < 3,  # no_misspelling
        !exists($self->dups->{$subject}),                # no_duplicate
    );

    if ($result{body_used})
    {
        $result{empty_second_line} = !length $body[0];
        $result{    body_limit   } = all { length $_ <= 72 } @body;
    }

    $self->dups->{$subject} = 1;

    return \%result
}


no Moose;
1
