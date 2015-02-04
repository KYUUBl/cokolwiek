import utils.AccountType;

/**
 * Created by Kamil on 2015-02-04.
 */
public class User {

        private String id;
        private AccountType accountType;

        public String getId() {
                return id;
        }

        public void setId(String id) {
                this.id = id;
        }

        public AccountType getAccountType() {
                return accountType;
        }

        public void setAccountType(AccountType accountType) {
                this.accountType = accountType;
        }


        public User(String id, AccountType accountType) {
                this.id = id;
                this.accountType = accountType;
        }



}
