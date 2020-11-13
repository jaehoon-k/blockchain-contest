pragma solidity >0.5.0;

import "./GSN/Context.sol";
import "./token/IERC20.sol";
import "./math/SafeMath.sol";


contract EVBatteryToken is Context, IERC20 {
    using SafeMath for uint256;

    mapping (address => uint256) private _balances;

    mapping (address => mapping (address => uint256)) private _allowances;

    mapping(address => Payment) private _deposits;

    enum PaymentStatus {Pending, Completed, Failure }

    struct Payment {
        PaymentStatus status;
        uint256 value;
    }

    uint256 private _totalSupply;

    string private _name;
    string private _symbol;
    uint8 private _decimals;

    constructor (string memory name, string memory symbol, uint8 decimals) public {
        _name = name;
        _symbol = symbol;
        _decimals = decimals;
    }

    event Deposited(address indexed to, uint256 indexed value);
    event StatusChanged(address indexed to, bool value);
    event Released(address indexed to, uint256 indexed value);
    event refunded(address indexed to);

    function getDeposit(address account)  public view returns (uint256)  {

        return _deposits[account].value;
    }

    function deposit(address seller, uint256 amount) public  {
        require(_balances[msg.sender]>=amount);
        _balances[msg.sender] = _balances[msg.sender].sub(amount);
        _deposits[seller].value = _deposits[seller].value.add(amount);
        _deposits[seller].status=PaymentStatus.Pending;
        emit Deposited(seller, amount);
    }

    function status(address seller, bool stat) public  {
        if(stat==true){
            _deposits[seller].status=PaymentStatus.Completed;
        }
        else{
            _deposits[seller].status=PaymentStatus.Failure;
        }
        emit StatusChanged(seller, stat);
    }

    function release(address seller) public{

        require(releaseAllowed(seller), "ConditionalEscrow: seller is not allowed to withdraw");
        uint256 amount = _deposits[seller].value;
        _deposits[seller].value = 0;
        _balances[seller] = _balances[seller].add(amount);
        emit Released(seller, amount);
    }

    function refund(address seller) public{
        
        uint256 amount = _deposits[seller].value;
        _deposits[seller].value = 0;
        _balances[msg.sender] = _balances[msg.sender].add(amount);
        emit refunded(seller);
    }

    function releaseAllowed(address seller) public returns (bool){
        Payment memory payment=_deposits[seller];
        if (payment.status == PaymentStatus.Completed) {
            return true;
        }
        else{
            return false;
        }
    }

    function totalSupply() public view returns (uint256) {
        return _totalSupply;
    }

    function balanceOf(address account) public view returns (uint256) {
        return _balances[account];
    }

    function transfer(address recipient, uint256 amount) public returns (bool) {
        _transfer(_msgSender(), recipient, amount);
        return true;
    }

    function allowance(address owner, address spender) public view returns (uint256) {
        return _allowances[owner][spender];
    }

    function approve(address spender, uint256 amount) public returns (bool) {
        _approve(_msgSender(), spender, amount);
        return true;
    }

    function transferFrom(address sender, address recipient, uint256 amount) public returns (bool) {
        _transfer(sender, recipient, amount);
        _approve(sender, _msgSender(), _allowances[sender][_msgSender()].sub(amount, "ERC20: transfer amount exceeds allowance"));
        return true;
    }

    function increaseAllowance(address spender, uint256 addedValue) public returns (bool) {
        _approve(_msgSender(), spender, _allowances[_msgSender()][spender].add(addedValue));
        return true;
    }

    function decreaseAllowance(address spender, uint256 subtractedValue) public returns (bool) {
        _approve(_msgSender(), spender, _allowances[_msgSender()][spender].sub(subtractedValue, "ERC20: decreased allowance below zero"));
        return true;
    }

    function _transfer(address sender, address recipient, uint256 amount) internal {
        require(sender != address(0), "ERC20: transfer from the zero address");
        require(recipient != address(0), "ERC20: transfer to the zero address");

        _balances[sender] = _balances[sender].sub(amount, "ERC20: transfer amount exceeds balance");
        _balances[recipient] = _balances[recipient].add(amount);
        emit Transfer(sender, recipient, amount);
    }

    function _mint(address account, uint256 amount) public {
        require(account != address(0), "ERC20: mint to the zero address");

        _totalSupply = _totalSupply.add(amount);
        _balances[account] = _balances[account].add(amount);
        emit Transfer(address(0), account, amount);
    }

    function _burn(address account, uint256 amount) internal {
        require(account != address(0), "ERC20: burn from the zero address");

        _balances[account] = _balances[account].sub(amount, "ERC20: burn amount exceeds balance");
        _totalSupply = _totalSupply.sub(amount);
        emit Transfer(account, address(0), amount);
    }

    function _approve(address owner, address spender, uint256 amount) internal {
        require(owner != address(0), "ERC20: approve from the zero address");
        require(spender != address(0), "ERC20: approve to the zero address");

        _allowances[owner][spender] = amount;
        emit Approval(owner, spender, amount);
    }

    function _burnFrom(address account, uint256 amount) internal {
        _burn(account, amount);
        _approve(account, _msgSender(), _allowances[account][_msgSender()].sub(amount, "ERC20: burn amount exceeds allowance"));
    }
}
