library(shiny)
shinyUI(pageWithSidebar(
    headerPanel("Example 2.1 (OR Book)"),
    sidebarPanel(
	selectInput("profitInput", "Select Profit Input:",c("User Interface" = "ui", "File" = "file")),
	numericInput("chairProfit", "Profit per chair:", 16, min = 0, max = 100),
	numericInput("deskProfit", "Profit per desk:", 20, min = 0, max = 100),
	numericInput("tableProfit", "Profit per table:", 14, min = 0, max = 100),
	#verbatimTextOutput("objective"),
	#downloadButton('dldat', 'Download Sample')
    	#Upload file
 fileInput('file1', 'Choose CSV File',
              accept=c('text/csv', 'text/comma-separated-values,text/plain')),
    checkboxInput('header', 'Header', TRUE),
    radioButtons('sep', 'Separator',
                 c(Comma=',',
                   Semicolon=';',
                   Tab='\t'),
                 'Comma'),
    radioButtons('quote', 'Quote',
                 c(None='',
                   'Double Quote'='"',
                   'Single Quote'="'"),
                 'Double Quote')
    ),
    mainPanel(
tabsetPanel(
tabPanel(h4("Problem 2.1"), 
tags$p("Brown Furniture Company makes three kinds of office furniture: chairs, desks, and tables. Each product requires skilled labor in the parts fabrication department, unskilled labor in the assembly department, machining on some key pieces of equipment, and some wood as raw material. At current prices, the unit profit contribution for each product is known, and the company can sell everything that it manufactures. The size of the workforce has been established, so the number of skilled and unskilled labor hours are known. The time available on the relevant equipment has also been determined/, and a known quantity of wood can be obtained each month under a contract with a wood supplier. Managers at Brown Furniture would like to maximize their profit contribution for the month by choosing production quantities for the chairs, desks, and tables."), 
h4("Results"), 
verbatimTextOutput("objective"), 
h4("Model"), 
tableOutput("modelTable")),
tabPanel(h4("Contents"),tableOutput("contents")),            
tabPanel(h4("Results"),verbatimTextOutput("summary"))
)
)
)
)
