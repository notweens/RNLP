public class Person {
    public String uuid;
    public String lastName;
    public String firstName;
    public String middleName;
    public String date;
    public String address;
    public String number;

    public Person(String uuid, String lastName, String firstName, String middleName, String date, String address, String number) {
        this.uuid = uuid;
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.date = date;
        while (address.contains("город") || address.contains("улица") || address.contains("кв")) {
            address = address.replace("город", "");
            address = address.replace("улица", "");
            address = address.replace("кв", "");
            char[] chars = address.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (i != 0 && chars[i] == 'д' && chars[i - 1] == ' ' && chars[i + 1] == ' ') {
                    address = address.substring(0, i) + "" + address.substring(i + 1);
                }
            }
        }
        this.address = address;
        this.number = number;
        System.out.println(address);
    }
}


