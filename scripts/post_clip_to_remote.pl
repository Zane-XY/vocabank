use strict;
use warnings;
use LWP::UserAgent;
use JSON;
use Config::Tiny;

#my @lines = <STDIN>;
my @lines = ("headword", "content");

my $config = Config::Tiny->read('config.ini');
my $email = $$config{_}{email};
my $pass =  $$config{_}{pass};
my $uri = $$config{_}{uri};

my ($headword, @context) = @lines;

sub send_to_remote {
  my $json = {headword => $headword, context => join('', @context), rating => 1};
  my $req = HTTP::Request->new('POST', $uri);
  $req->header('Content-Type' => 'application/json');
  $req->context(JSON->new->encode($json));
  $req->authorization_basic($email, $pass);

  my $lwp = LWP::UserAgent->new;
  $lwp->request($req);
}

if(@context){
    send_to_remote();
} else {
    print "skipped due to empty content", "\n";
}

