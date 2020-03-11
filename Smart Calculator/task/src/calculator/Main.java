package calculator;

import java.util.*;
import java.util.regex.*;
import java.math.BigInteger;

public class Main {

    private static HashMap<String, BigInteger> variables = new HashMap<>();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        // put your code here
        while (true) {
            String input = sc.nextLine();
            if ( input.length() == 0 ) continue;
            if ( input.charAt(0) == '/' ) {
                command(input);
            }
            else calculate(input);
        }
    }

    private static void command(String command) {
        if ( "/exit".equals(command) ) {
            System.out.println("Bye!");
            System.exit(0);
        }
        else if ( "/help".equals(command) ) {
            System.out.println("This is a smart calculator");
        }
        else {
            System.out.println("Unknown command");
        }
    }

    private static void calculate(String input) {
        if ( input.contains("=") ) {
            assignment(input.split("\\s*=\\s*"));
        }
        else if ( input.split("\\s+").length == 1 && (isValidName(input) || isValidNumber(input)) ) {
            singleInput(input.split("\\s+")[0]);
        }
        else {
            postfixCalculate(input);
        }
    }

    private static void postfixCalculate(String input) {
        Pattern pattern = Pattern.compile("\\s+");
        Matcher matcher = pattern.matcher(input);
        input = matcher.replaceAll("");

        String postfix = infixToPostfix(input);
        if ( "".equals(postfix) ) return;
        String[] ops = postfix.split(" ");
        Deque<String> stack = new ArrayDeque<>();
        for (String op : ops) {
            if (isValidNumber(op)) stack.offerLast(op);
            else if (isValidName(op))
                if ( isInitialized(op) ) stack.offerLast(variables.get(op).toString());
                else {
                    System.out.println("Unknown variable"); return;
                }
            else if (isOperator(op)) {
                BigInteger op1 = new BigInteger(stack.pollLast());
                BigInteger op2 = new BigInteger(stack.pollLast());
                BigInteger result = BigInteger.ZERO;
                switch (op) {
                    case "+":
                        result = op1.add(op2);
                        break;
                    case "-":
                        result = op2.subtract(op1);
                        break;
                    case "*":
                        result = op1.multiply(op2);
                        break;
                    case "/":
                        result = op2.divide(op1);
                        break;
                    case "^":
                        result = op2.pow(op1.intValue());
                    default:
                        break;
                }
                stack.offerLast(result.toString());
            }
        }
        System.out.println(stack.pollLast());
    }

    private static void singleInput(String input) {
        if ( isValidName(input) ) {             //input is a variable name
            if ( variables.containsKey(input) ) {
                System.out.println(variables.get(input));
            }
            else {
                System.out.println("Unknown variable");
            }
        }
        else if ( isValidNumber(input) ) {      //input is a single number
            System.out.println(input);
        }
        else {
            System.out.println("Invalid expression");
        }
    }

    private static void assignment(String[] operands) {
        if ( operands.length != 2 || (!isValidNumber(operands[1]) && !isValidName(operands[1])) ) {
            System.out.println("Invalid assignment");
        }
        else if ( !isValidName(operands[0]) ) {
            System.out.println("Invalid identifier");
        }
        else if ( isValidName(operands[1]) ) {
            if ( variables.containsKey(operands[1]) ) {
                variables.put(operands[0], variables.get(operands[1]));
            }
            else {
                System.out.println("Unknown variable");
            }
        }
        else {
            variables.put(operands[0], new BigInteger(operands[1]));
        }
    }

    private static String infixToPostfix(String input) {
        String postfix = "";
        Deque<String> stack = new ArrayDeque<>();
        for( int i = 0; i < input.length(); i++ ) {
            String currChar = Character.toString(input.charAt(i));
            if ( isValidNumberWithoutPrefix(currChar) ) {
                String result = getNumber(input, i);
                postfix += result + " ";
                i += result.length() - 1;
            }
            else if ( isValidName(currChar) ) {
                String result = getVariable(input, i);
                postfix += result + " ";
                i += result.length() - 1;
            }
            else if ( isOperator(currChar) ) {
                if ( precedence(currChar) > precedence(stack.peekLast()) || stack.isEmpty()
                        || "(".equals(stack.peekLast())) {
                    if ( "+".equals(currChar) || "-".equals(currChar) ) {
                        int result = plusMinus(input, currChar, i);
                        if ( result > 0 ) stack.offerLast("+");
                        else if ( result < 0 ) stack.offerLast("-");
                        i += Math.abs(result) - 1;
                    }
                    else if ( currChar.equals(Character.toString(input.charAt(i+1))) ) {
                        System.out.println("Invalid expression");
                        return "";
                    }
                    else {
                        stack.offerLast(currChar);
                    }
                }
                else {
                    while ( !stack.isEmpty() && precedence(stack.peekLast()) >= precedence(currChar)
                            && !"(".equals(stack.peekLast()) ) {
                        postfix += stack.pollLast() + " ";
                    }
                    if ( "+".equals(currChar) || "-".equals(currChar) ) {
                        int result = plusMinus(input, currChar, i);
                        if ( result > 0 ) stack.offerLast("+");
                        else if ( result < 0 ) stack.offerLast("-");
                        i += Math.abs(result) - 1;
                    }
                    else if ( currChar.equals(Character.toString(input.charAt(i+1))) ) {
                        System.out.println("Invalid expression");
                        return "";
                    }
                    else {
                        stack.offerLast(currChar);
                    }
                }
            }
            else if ( "(".equals(currChar) ) {
                stack.offerLast("(");
            }
            else if ( ")".equals(currChar) ) {
                while ( !stack.isEmpty() && !"(".equals(stack.peekLast()) ) {
                    postfix += stack.pollLast() + " ";
                }
                if ( stack.isEmpty() ) {
                    System.out.println("Invalid expression");
                    return "";
                }
                stack.pollLast();
            }
        }
        while ( !stack.isEmpty() ) {
            if ( "(".equals(stack.peekLast()) || ")".equals(stack.peekLast()) ) {
                System.out.println("Invalid expression");
                return "";
            }
            postfix += stack.pollLast() + " ";
        }
        return postfix;
    }

    private static String getNumber(String input, int position) {
        String number = Character.toString(input.charAt(position));
        int currIndex = position + 1;
        while ( currIndex < input.length() &&
                isValidNumberWithoutPrefix(number + Character.toString(input.charAt(currIndex))) ) {
            number += Character.toString(input.charAt(currIndex));
            currIndex++;
        }
        return number;
    }

    private static String getVariable(String input, int position) {
        String var = Character.toString(input.charAt(position));
        int currIndex = position + 1;
        while ( currIndex < input.length() &&
                isValidName(var + Character.toString(input.charAt(currIndex))) ) {
            var += Character.toString(input.charAt(currIndex));
            currIndex++;
        }
        return var;
    }

    private static int plusMinus(String input, String currChar, int position) {
        int count = 0;
        if ( "+".equals(currChar) ) {
            while ( position < input.length() &&
                    "+".equals(Character.toString(input.charAt(position)))) {
                position++; count++;
            }
            return count;
        }
        else {
            while ( position < input.length() &&
                    "-".equals(Character.toString(input.charAt(position)))) {
                position++; count++;
            }
            if (count % 2 == 0) return count;
            else return count * -1;
        }
    }

    private static int precedence(String op) {
        if ( "+".equals(op) || "-".equals(op) ) return 1;
        if ( "*".equals(op) || "/".equals(op) ) return 2;
        if ( "^".equals(op) ) return 3;
        return -1;
    }

    private static boolean isInitialized(String variable) {
        return variables.containsKey(variable);
    }

    private static boolean isValidName(String variableName) {
        Pattern pattern = Pattern.compile("[a-zA-Z]+");
        Matcher matcher = pattern.matcher(variableName);
        return matcher.matches();
    }

    private static boolean isValidNumber(String input) {
        Pattern pattern = Pattern.compile("[+-]?\\d+");
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    private static boolean isValidNumberWithoutPrefix(String input) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    private static boolean isOperator(String input) {
        Pattern pattern = Pattern.compile("[+\\-*/]");
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }
}