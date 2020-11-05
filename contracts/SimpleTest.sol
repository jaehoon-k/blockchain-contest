pragma solidity ^0.5.8;

contract SimpleTest {

    uint count;

    function increase() public {
        count += 1;
    }

    function getCount() public view returns(uint){
        return count;
    }
}
