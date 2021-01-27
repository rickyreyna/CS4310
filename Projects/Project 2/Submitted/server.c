// Ricardo Reyna
// rjr110


#include <netinet/in.h>
#include <netdb.h>
#include <arpa/inet.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdbool.h>
#include <string.h>
#include <time.h>


#define MAXWRITE 1024
#define MAXBUF 1024
#define MAXSERVERBACKLOG 5 // upper bound on the number of incoming connections that can be waiting for the server
#define MAXCONNECTIONS 5
#define MAXADDRESS 16

int main (int argc,char* argv[]) {
    int port = atoi(argv[1]);
    printf("Setting the server on port: %i", port);
    printf("\n");

    // Creates files
    FILE *logFile, *readFile;
    logFile = fopen("log.txt", "w");

    char *agentsConnected[MAXCONNECTIONS][MAXADDRESS] = {}; // List of current agents
    time_t *connectionTimes[MAXCONNECTIONS] = {}; // List of connection times for each agent

    int serverSocket, agentSocket, agentIndex = 0, agentLength;

    // SOCK_STREAM specifies a socket with reliable byte-stream semantics
    // AF_INET - Address Family Internet
    // 0 is the type of protocol - it asks the OS to specify the port
    // Returns a non-negative value for success and -1 for failure
    serverSocket = socket(AF_INET, SOCK_STREAM, 0);
    // Checking is server socket was created
    if(serverSocket < 0) {
        printf("Socket creation failed..\n");
        exit(EXIT_FAILURE);

    }
    else {
        printf("Socket was successfully created.. \n");
    }

    struct sockaddr_in serverAddress, agentAddress;
    // Setting the server address
    serverAddress.sin_family = AF_INET;
    serverAddress.sin_port = htons(port);
    serverAddress.sin_addr.s_addr = INADDR_ANY;

    // Had problem binding socket, stackoverflow answer fixed it.
    // https://stackoverflow.com/questions/22126940/c-server-sockets-bind-error
    int opt = 1;
    if (setsockopt(serverSocket, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt)) < 0) {
        printf("Issue when reusing socket\n");
        close(serverSocket);
        exit(EXIT_FAILURE);
    }

    // Bind takes the port and IP to find them into the file descriptor (a socket), so that the
    // traffic that comes can be read by the file descriptor.
    // We will check if the binding worked
    if(bind(serverSocket, (struct sockaddr*) &serverAddress, sizeof(serverAddress)) < 0) {
        printf("Socket bind failed..\n");
        close(serverSocket);
        exit(EXIT_FAILURE);
    }
    else {
        printf("Socket is binded..\n");
    }

    // Now, we need to make the sever start listening
    printf("The server is now listening for Agents...\n");
    // The second parameter is the size of the queue backlog.
    // It specifies the upper bound on the number of incoming connections that can be waiting at any time.
    // Since our server is single threaded, 5 will suffice.
    // listen returns 0 on success and -1 on failure
    if(listen(serverSocket, MAXSERVERBACKLOG) < 0 ) {
        printf("Server listening failed..\n");
        close(serverSocket);
        exit(EXIT_FAILURE);
    }


    while(1) {
        time_t currentTime;
        agentLength = sizeof(agentAddress);
        /* dequeues the next connection on the queue for socket. If the queue is empty,
         * accept() blocks until a connection request arrives. When successful, accept() fills in the
         * sockaddr structure, pointed to by clientAddress, with the address of the client at the other
         * end of the connection, addressLength specifies the maximum size of the clientAddress address
         * structure and contains the number of bytes actually used for the address upon return.
         */
        agentSocket = accept(serverSocket, (struct sockaddr*) &agentAddress, &agentLength);
        // Checks that the agent was accepted
        if(agentSocket < 0) {
            printf("Failed to connect to agent..\n");
            close(serverSocket);
            exit(EXIT_FAILURE);
        }
        else {
            printf("Connection to agent is successful..\n");
        }

        char buffer[MAXBUF];
        char agentAddressText[MAXADDRESS];
        // If successful, read() returns the number of bytes actually read and placed in buffer
        int agentMessage = read(agentSocket, buffer, MAXBUF);
        // Check if data was read
        if(agentMessage < 0) {
            printf("Failed to read agent data..\n");
            close(serverSocket);
            close(agentSocket);
            exit(EXIT_FAILURE);
        }
        //Converts agent address structure into a character string. Saves agent ip into string
        inet_ntop(AF_INET, &agentAddress.sin_addr, agentAddressText, sizeof(agentAddressText));

        bool found = false; // Flag used to found agent
        int i;
        for(i = 0; i < MAXCONNECTIONS; ++i) {
            if(!strcmp(agentsConnected[i], agentAddressText)) {
                agentIndex = i;
                found = true;
            }
        }
        if(!found) {
            agentIndex = -1;
        }

        if((strcmp(buffer, "#JOIN")) == 0) {
            currentTime = time(0); // Set variable to the current time
            char temp[MAXBUF];
            strftime(temp, sizeof(temp), "%a %Y-%m-%d %H:%M:%S %Z", localtime(&currentTime));
            fprintf(logFile, "%s: Received a \"#JOIN\" action from agent \"%s\"", temp, agentAddressText);
            fprintf(logFile, "\n\n");
            fflush(logFile); // Clears output buffer

            if(agentIndex < 0) {
                bool filled = true;
                for(i = 0; i < MAXCONNECTIONS; ++i) {
                    if (agentsConnected[i][0] == 0) {
                        filled = false;
                        sprintf(agentsConnected[i], "%s", agentAddressText);
                        connectionTimes[i] = time(0);
                        break;

                    }
                }
                    if(filled) {
                        write(agentSocket, "$THE SERVER IS FULL", 19);
                        currentTime = time(0); // Set variable to the current time
                        char temp[MAXBUF];
                        strftime(temp, sizeof(temp), "%a %Y-%m-%d %H:%M:%S %Z", localtime(&currentTime));
                        fprintf(logFile, "%s: Responded to agent \"%s\" THE SERVER IS FULL", temp, agentAddressText);
                        fprintf(logFile, "\n\n");
                        fflush(logFile);
                    }
                    else {
                        char temp[MAXBUF];
                        strftime(temp, sizeof(temp), "%a %Y-%m-%d %H:%M:%S %Z", localtime(&currentTime));
                        fprintf(logFile, "%s: Responded to agent \"%s\" with \"#OK\"", temp, agentAddressText);
                        fprintf(logFile, "\n\n");
                        fflush(logFile); // Clears output buffer
                        write(agentSocket, "$OK", MAXWRITE);
                    }

            }
            else {
                char temp[MAXBUF];
                strftime(temp, sizeof(temp), "%a %Y-%m-%d %H:%M:%S %Z", localtime(&currentTime));
                fprintf(logFile, "%s: Responded to agent \"%s\" with \"#ALREADY MEMBER\"", temp, agentAddressText);
                fprintf(logFile, "\n\n");
                fflush(logFile); // Clears output buffer
                write(agentSocket, "#ALREADY MEMBER", 15);

            }
        }
        else if((strcmp(buffer, "#LEAVE")) == 0) {
            currentTime = time(0); // Set variable to the current time
            char temp[MAXBUF];
            strftime(temp, sizeof(temp), "%a %Y-%m-%d %H:%M:%S %Z", localtime(&currentTime));
            fprintf(logFile, "%s: Received a \"#LEAVE\" action from agent \"%s\"", temp, agentAddressText);
            fprintf(logFile, "\n\n");
            fflush(logFile); // Clears output buffer

            if(agentIndex < 0) {
                write(agentSocket, "#NOT MEMBER", 13);
            }
            else {
                memset(agentsConnected[agentIndex], 0, MAXADDRESS);
                write(agentSocket, "#OK", 3);
            }
        }
        else if((strcmp(buffer, "#LIST")) == 0) {
            currentTime = time(0); // Set variable to the current time
            char temp[MAXBUF];
            strftime(temp, sizeof(temp), "%a %Y-%m-%d %H:%M:%S %Z", localtime(&currentTime));
            fprintf(logFile, "%s: Received a \"#LIST\" action from agent \"%s\"", temp, agentAddressText);
            fprintf(logFile, "\n\n");
            fflush(logFile); // Clears output buffer
            if (agentIndex >= 0) {
                char temp[MAXBUF];
                strftime(temp, sizeof(temp), "%a %Y-%m-%d %H:%M:%S %Z", localtime(&currentTime));
                fprintf(logFile, "%s: Responded to agent \"%s\" with \" the current list\"", temp, agentAddressText);
                fprintf(logFile, "\n\n");
                fflush(logFile); // Clears output buffer

                char listOfAgents[MAXWRITE] = {};
                int i;
                for (i = 0; i < MAXCONNECTIONS; ++i) {
                    long num = connectionTimes[i];
                    currentTime = time(NULL);
                    num = currentTime - num;
                    if (agentsConnected[i][0] != 0) {
                        currentTime = time(NULL);
                        sprintf(listOfAgents, "<%s, %ld> \n", agentsConnected[i], num);
                        write(agentSocket, listOfAgents, MAXWRITE);
                    }
                }
            }
            else {
                char temp[MAXBUF];
                strftime(temp, sizeof(temp), "%a %Y-%m-%d %H:%M:%S %Z", localtime(&currentTime));
                fprintf(logFile, "%s: No response supplied to agent \"%s\"", temp, agentAddressText);
                fprintf(logFile, "\n\n");
                fflush(logFile); // Clears output buffer
            }
        }
        else if(strcmp(buffer, "#LOG") == 0) {
            currentTime = time(0); // Set variable to the current time
            char temp[MAXBUF];
            strftime(temp, sizeof(temp), "%a %Y-%m-%d %H:%M:%S %Z", localtime(&currentTime));
            fprintf(logFile, "%s: Received a \"#LOG\" action from agent \"%s\"", temp, agentAddressText);
            fprintf(logFile, "\n\n");
            fflush(logFile); // Clears output buffer
            if(agentIndex >= 0) {
                char temp[MAXBUF];
                strftime(temp, sizeof(temp), "%a %Y-%m-%d %H:%M:%S %Z", localtime(&currentTime));
                fprintf(logFile, "%s: Responded to agent \"%s\" with \" with the current log.txt \"", temp, agentAddressText);
                fprintf(logFile, "\n\n");
                fflush(logFile); // Clears output buffer

                readFile = fopen("log.txt", "r");
                memset(buffer,0,MAXBUF);
                if(readFile == NULL) {
                    printf("Failed to open the file");
                }
                else {
                    while(fgets(buffer,MAXWRITE -1, readFile)) {
                        write(agentSocket, buffer, strlen(buffer));
                    }
                    fclose(readFile); // close the file
                }
            }
            else {
                char temp[MAXBUF];
                strftime(temp, sizeof(temp), "%a %Y-%m-%d %H:%M:%S %Z", localtime(&currentTime));
                fprintf(logFile, "%s: No response supplied to agent \"%s\"", temp, agentAddressText);
                fprintf(logFile, "\n\n");
                fflush(logFile); // Clears output buffer
            }
        }
        close(agentSocket);// when server is done socket needs to close
        memset(&agentAddress, 0, sizeof(agentAddress));
        fflush(logFile);
    }

    return 0;
}