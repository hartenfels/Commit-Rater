$script = <<SCRIPT
sudo apt-get install -y carton
sudo apt-get install -y perl-doc
sudo apt-get install -y git
git config --global user.name "vagrant"
git config --global user.email "vagrant@vagrant.vg"
SCRIPT


Vagrant.configure(2) do |config|
  config.vm.box = "ubuntu/trusty64"
  config.vm.provision "shell", inline: $script
end
