package CommitRater::Repo;
use Moose;
use Encode                qw(decode);
use File::Path            qw(remove_tree);
use File::Spec::Functions qw(catfile);
use CommitRater::Git::Repository;


has remote => (
    is       => 'ro',
    isa      => 'Str',
    required => 1,
);

has local => (
    is       => 'ro',
    isa      => 'Str',
    lazy     => 1,
    default  => sub { catfile '__repos/', shift->remote =~ s/\W/_/gr },
);

has repo => (
    is      => 'ro',
    isa     => 'CommitRater::Git::Repository',
    lazy    => 1,
    clearer => 'clear_repo',
    default => sub
    {   CommitRater::Git::Repository->new(work_tree => shift->local) },
);


sub clone
{
    my ($self) = @_;
    CommitRater::Git::Repository->git(qw(clone -nq), $self->remote,
                                                     $self->local);
}

sub pull
{
    my ($self) = @_;
    $self->repo->git(qw(pull -q));
}

sub update
{
    my ($self) = @_;

    my $local = $self->local;
    if (-e $local)
    {
        # try to pull
        return if eval { $self->pull; 1 };
        # didn't work, delete the repo and re-clone
        $self->clear_repo;
        remove_tree($local);
        die "Pull failed and can't remove '$local' to re-clone\n" if -e $local;
    }

    $self->clone;
}


sub each_commit
{
    my ($self, $callback, $limit) = @_;

    my @cmd = ('log', '--format=%H%n%aE%n%P%n%B%n%x00');
    push @cmd, -n => $limit if $limit;
    my $log = decode('UTF-8', scalar $self->repo->git(@cmd));

    while ($log =~ /(.*?)\n(.*?)\n(.*?)\n(.*?)\0\n?/gs)
    {
        local $_ = {
            sha     => $1,
            email   => $2,
            parents => [split ' ', $3],
            message => [split /\n/, $4],
        };
        $callback->();
    }
}


no Moose;
1
