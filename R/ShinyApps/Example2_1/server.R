library(shiny)
library(lpSolve)
library(lpSolveAPI)
# Define an LP problem
shinyServer(function(input, output) {
   #Show file contents
   output$contents <- renderTable({
    
    # input$file1 will be NULL initially. After the user selects and uploads a 
    # file, it will be a data frame with 'name', 'size', 'type', and 'datapath' 
    # columns. The 'datapath' column will contain the local filenames where the 
    # data can be found.
    inFile <- input$file1

    if (is.null(inFile)){
      return(NULL)
    }
    # output$chairProfit <- 99
    read.csv(inFile$datapath, header=input$header, sep=input$sep, quote=input$quote)
   })



   # Return the objective result
   output$objective <- reactive({
	# Set the number of vars
	model <- make.lp(0, 3)
	# Define the object function: for Minimize, use -ve
	set.objfn(model, c(input$chairProfit, input$deskProfit, input$tableProfit))
	# Add the constraints
	add.constraint(model, c(4, 6, 2), "<=", 2000)
	add.constraint(model, c(3, 8, 6), "<=", 2000)
	add.constraint(model, c(9, 6, 4), "<=", 1440)
	add.constraint(model, c(30, 40, 25), "<=", 9600)
	# Set the upper and lower bounds
	set.bounds(model, lower=c(0, 0, 0), upper=c(10000, 10000, 10000))
	#Set to maximize
	lp.control(model,sense='max')
	write.lp(model,'model.csv',type='lp')
	#Write model
	### write.lp(model,'model.lp',type='lp')
	# Compute the optimized model
	solve(model)
	# Get the value of the optimized parameters
	# $optParams <- get.variables(model)
	# Get the value of the objective function
	paste("Objective:\n", get.objective(model),"\n\n","Optminized Values:\n","Chairs:", get.variables(model)[1],"Desks:", get.variables(model)[2],"Tables:", get.variables(model)[3],"\n\n","Constraints:\n","Fabrication:",get.constraints(model)[1],"Assembly:",get.constraints(model)[2],"Machining:",get.constraints(model)[3],"Wood:",get.constraints(model)[4])
	#rbind(get.objective(model), get.variables(model),get.constraints(model))	
# Get the value of the constraint
	#get.constraints(localmodel)
   })

   output$modelTable <- renderTable({
	d = read.table("~/R/x86_64-pc-linux-gnu-library/2.15/shiny/examples/Example2_1/model.csv", sep="\t", fill=FALSE, strip.white=TRUE)
	   # head(datasetInput(), n = input$obs)
  })   
})
