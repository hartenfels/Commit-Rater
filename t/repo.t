use Test::Most   qw(bail);
no warnings      qw(redefine);
use File::Temp;
use Git::Repository;
use Scalar::Util qw(blessed);
use CommitRater::Repo;


sub run_git
{
    my $repo = blessed $_[0] ? shift : 'Git::Repository';
    $repo->run(@$_, {fatal => '!0'}) for @_;
}

sub spew
{
    my $file = shift;
    open my $fh, '>', $file or die "open '$file': $!\n";
    print {$fh} $_ for @_;
    close $fh or die "close '$file': $!\n";
}


my   $test_dir = File::Temp->newdir;
my  $local_dir = "$test_dir/local";
my $remote_dir = "$test_dir/remote";
my  $clone_dir = "$test_dir/clone";


run_git(
    [qw(init),         $local_dir],
    [qw(init --bare), $remote_dir],
);
my $local  = Git::Repository->new(work_tree =>  $local_dir);
my $remote = Git::Repository->new(work_tree => $remote_dir);


my @files = qw(test1 test2 test3);
spew("$local_dir/$_", "$_\n") for @files;


run_git($local,
    [qw(config --local user.name  commit-rater)],
    [qw(config --local user.email commit-rater@example.com)],
    [qw(add), @files],
    [qw(commit -qm), 'Add test files'],
    [qw(remote add origin), $remote_dir],
    [qw(push -qu origin master)],
);

# stash subroutines so they can be patched away
my $clone = \&CommitRater::Repo::clone;
my $pull  = \&CommitRater::Repo::pull;


my $repo = CommitRater::Repo->new(remote => $remote_dir, local => $clone_dir);
*CommitRater::Repo::pull = sub { die }; # we don't want to be pulling right now
lives_ok { $repo->update } 'clone repo via update';


*CommitRater::Repo::pull  = $pull;
*CommitRater::Repo::clone = sub { die }; # now we don't want to be cloning
lives_ok { $repo->update } 'pull unchanged repo via update';

spew("$local_dir/test4", "test4\n");
run_git($local,
    [qw(add -A)],
    [qw(commit -qm), 'Add another test file'],
    [qw(push -q)],
);
lives_ok { $repo->update } 'pull modified repo via update';


run_git($local,
    [qw(checkout -q --orphan dummy)],
    [qw(add -A)],
    [qw(commit -qm), 'Destroy everything'],
    [qw(branch -D master)],
    [qw(branch -m master)],
    [qw(push -qf origin master)],
);
*CommitRater::Repo::remove_tree = sub {};
throws_ok { $repo->update } qr/Pull failed and can't remove/,
          'failing pull and not being able to delete repo dir dies';


*CommitRater::Repo::clone       = $clone;
*CommitRater::Repo::remove_tree = \&File::Path::remove_tree;
spew("$clone_dir/sentinel", "this should get deleted\n");

lives_ok { $repo->update } 're-clone broken repo';
ok !-e "$clone_dir/sentinel", 'broken repo is wiped after re-clone';


done_testing
