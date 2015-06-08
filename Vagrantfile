$script = <<SCRIPT
sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
sudo apt-get install -y oracle-java8-installer oracle-java8-set-default carton perl-doc git
git config --global user.name "vagrant"
git config --global user.email "vagrant@vagrant.vg"
SCRIPT


Vagrant.configure(2) do |config|
  config.vm.box = "ubuntu/trusty64"
  config.vm.provision "shell", inline: $script
end
