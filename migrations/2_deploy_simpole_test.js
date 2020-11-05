const SimpleTest = artifacts.require("SimpleTest");

module.exports = function(deployer) {
  deployer.deploy(SimpleTest);
};
