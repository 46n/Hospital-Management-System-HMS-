package labs.lab02;

import java.util.Scanner;

public class Lab2_1 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Question 3: "Read an integer between 0 and 1000 and add all its digits."
        {
            System.out.println("Please enter a number between 1 - 1000: ");
            int num = sc.nextInt();
            while(num < 1 || num > 1000){
                System.out.println("Please re-enter a number between 1 - 1000 again: ");
                num = sc.nextInt();
            }

            int sumOfNum = 0;
            int numberOfDigit = String.valueOf(num).length();

            switch(numberOfDigit){
                case 1: sumOfNum = num;
                    break;
                case 2: sumOfNum = num / 10 + num % 10;
                    break;
                case 3: sumOfNum = num / 100 + num % 100 / 10 + num % 10;
                    break;
                case 4: sumOfNum = 1;
                    break;
            }
            System.out.println(sumOfNum);
        }

        // Question 4: "Convert an uppercase letter to a lowercase letter."
        {
            System.out.println("Enter a string in uppercase here: ");
            String str = sc.next();
            System.out.println("String in lowercase: " + str.toLowerCase());
        }

        // Question 5: "Receive an ASCII code and display its character."
        {
            System.out.println("Please enter a number between 0 - 128: ");
            int num = sc.nextInt();
            while(num < 9 || num > 128){
                System.out.println("Please re-enter a number between 0 - 128 again: ");
                num = sc.nextInt();
            }

            char character = (char)num;
            System.out.println(character);
        }

        // Question 6: "Enter the month and year and display the number of days."
        {
            System.out.println("Please enter year here: ");
            int year = sc.nextInt();
            System.out.println("Please enter month here: ");
            int month = sc.nextInt();
            int day = 0;
            switch(month){
                case 1, 3, 5, 7, 8, 10, 12: day = 31;
                    break;
                case 4, 6, 9, 11: day = 30;
                    break;
                case 2: if(year % 4 == 0 && year % 100 != 0){
                    day = 29;
                }else if(year % 4 == 0 && year % 400 == 0){
                    day = 29;
                }else{
                    day = 28;
                }
                    break;
                default: System.out.println("Invalid days");
            }
            System.out.println(day);
        }

        // Question 7: "Enter assignment marks and display the corresponding grade."
        {
            System.out.println("Enter your mark here: ");
            int mark = sc.nextInt();
            String grade = null;
            if(mark >= 0 && mark < 40){
                grade = "F";
            }else if(mark >= 40 && mark < 50){
                grade = "F+";
            }else if(mark >= 50 && mark < 55){
                grade = "D";
            }else if(mark >= 55 && mark < 65){
                grade = "C";
            }else if(mark >= 65 && mark < 70){
                grade = "B";
            }else if(mark >= 70 && mark < 75){
                grade = "B+";
            }else if(mark >= 75 && mark < 80){
                grade = "A";
            }else if(mark >= 80 && mark <= 100){
                grade = "A+";
            }else{
                grade = "Invalid mark";
            }
            System.out.println("Grade: " + grade);
        }
    }
}
