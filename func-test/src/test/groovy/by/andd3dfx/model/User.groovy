package by.andd3dfx.model

class User {
    def name
    def password

    User(name, password) {
        this.name = name
        this.password = password
    }

    @Override
    String toString() {
        return "User{name=$name}";
    }
}
