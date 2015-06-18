package CommitRater;
use Moose;


has repo => (
    is       => 'ro',
    isa      => 'CommitRater::Repo',
    required => 1,
);


sub rate
{
    my ($self) = @_;
    $self->repo->update;
    $self->repo->each_commit(sub { $self->rate_commit($_) });
}


sub rate_commit
{
    my ($self, $commit) = @_;
    print "$commit->{message}\n";
}


no Moose;
1
