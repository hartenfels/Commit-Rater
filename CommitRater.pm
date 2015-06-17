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
    $self->repo->each_commit(sub { print "$_->{sha}\n" });
}


no Moose;
1
