function validatelogin() {
  if (document.rec.username.value == "") {
    alert("Enter UserName ");
    document.rec.username.focus();
    return false;
  }

  if (document.rec.username.value.length > 15) {
    alert("Do not enter more than 15 characters");
    document.rec.username.focus();
    return false;
  }

  if (!isNaN(document.rec.username.value)) {
    alert("Enter Only alphabets ");
    document.rec.username.focus();
    return false;
  }

  if (document.rec.password.value == "") {
    alert("Enter password");
    document.rec.password.focus();
    return false;
  }

  if (document.rec.password.value.length < 6) {
    alert("The password should not be less than 6 characters");
    document.rec.password.focus();
    return false;
  }
}
