package by.andd3dfx.configs

import by.andd3dfx.model.Role
import by.andd3dfx.model.User

class Configuration {
    private static final String host = "localhost"

    public static final Role ottRole = new Role(
            'OTT', "http://$host:9090", 'entitlements', 'ott')
    public static final Role mvpdRole = new Role(
            'MVPD', "http://$host:9095", 'entitlements', 'mvpd')

    public static final User ottAlice = new User('alice', 'ott')
    public static final User ottBob = new User('bob', 'ott')
    public static final User ottClara = new User('clara', 'ott')

    public static final User mvpdAlice = new User('alice', 'mvpd')
    public static final User mvpdBob = new User('bob', 'mvpd')
    public static final User mvpdClara = new User('clara', 'mvpd')
}
