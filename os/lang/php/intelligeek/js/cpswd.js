function validatechange() {
  if (document.chg.oldpwsd.value == "") {
    alert("Enter  OldPassword ");
    document.chg.oldpwsd.focus();
    return false;
  }
  if (document.chg.newpwsd.value == "") {
    alert("Enter  NewPassword ");
    document.chg.oldpwsd.focus();
    return false;
  }
}
