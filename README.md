# oscar

## What is Oscar?

More than 15 million GP appointments are missed annually, costing the NHS hundreds of millions of pounds.


We have attempted to reduce this wastage with Oscar, a natural language email assistant to confirm attendence to GP appointments. We aim for Oscar to learn over time how to handle conversations more effectively and become personalised based on patient's input. 

## How to use Oscar

### Connecting to the Database

We are hosting the databse on srcf.net and are in the process of enabling Oscar to connect without prior set-up. Currently to run Oscar SSH port forwarding should be set up in advance by using in a terminal:

    ssh -L 9876:localhost:3306 [username]@shell.srcf.net

Where `[username]` is the username of an srcf.net account. You will then be prompted to enter the password for the associated account.