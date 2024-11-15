package by.andd3dfx.model

class User {
    String name
    String password

    User(String name, String password) {
        this.name = name
        this.password = password
    }

    @Override
    String toString() {
        return "User{name=$name}";
    }
}
