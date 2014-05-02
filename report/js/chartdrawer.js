function ChartDrawer(placeholder, densitydata) {
	var self = this;
	this.placeholder = placeholder;
	this.densitydata = densitydata;
	this.flotChart = 0;
	this.data = [];
	this.ticks = [];
	
	var __construct = function() {
		$.each(	self.densitydata,
				function(index, badSmellCountObj) {
					self.data.push({data: [[index, badSmellCountObj.count]], label: badSmellCountObj.type});
					self.ticks.push([index, badSmellCountObj.type]);
				}
		);
	}();
	
	this.optionsBar = {
		series: {
			bars: {
				show: true,
				fill: true,
				//lineWidth: 3,
				fillColor: { colors: [ { opacity: 0.75 }, { opacity: 0.75 } ] }
			},
			valueLabels: {
				show: true,
				showAsHtml: true,
				align: "center",
				labelFormatter: function(v) {
					return v;
					//return "" + Math.round(v/sum*100) + "%";
				}
			}
		},
		bars: {
			align: "center",
			barWidth: 0.5
		}, 
		xaxis: {
			//axisLabel: "Bad Smell Type",
			//rotateTicks: 160,
			//axisLabelUseCanvas: true,
			//axisLabelFontSizePixels: 12,
			//axisLabelFontFamily: 'Verdana, Arial',
			//axisLabelPadding: 10,
			ticks: self.ticks,
			autoscaleMargin: 0.02
		}, 
		yaxis: {
			axisLabel: "Number of Bad Smells",
			axisLabelUseCanvas: false,
			axisLabelFontSizePixels: 12,
			axisLabelFontFamily: 'Verdana, Arial',
			axisLabelPadding: 3
		},
		legend: {
			show: false
		},
		grid: {
			hoverable: true
		},
		tooltip: true,
		tooltipOpts: {
			content: "<span id='bscount'>%y</span> %x",
			shifts: {
				x: 20,
				y: 0
			},
			defaultTheme: false
		}
	};

	this.optionsPie = {
		series: {
			pie: {
				show: true,
				innerRadius: 0.5,
				radius: 1,
				label: {
					show: true,
					radius: 3/4,
					formatter: function(label, series) {
						return '<div id="tooltipPie">'+Math.round(series.percent)+'%</div>';
					},
					background: { 
						opacity: 0.5,
						color: '#000'
					}
				}
			}			
		},
		legend: {
				show: true,
				margin: [50, 0]
			},
		grid: {
			hoverable: true
		},
		tooltip: true,
		tooltipOpts: {
			content: "<span id='bscount'>%s</span>",
			shifts: {
				x: 20,
				y: 0
			},
			defaultTheme: false
		}
	};
	
	this.drawBar = function() {
		self.flotChart = $.plot(this.placeholder, this.data, this.optionsBar);
	}
	
	this.drawPie = function() {
		self.flotChart = $.plot(this.placeholder, this.data, this.optionsPie);
	}
	
	this.bindToggle = function(toggle) {
		self.toggle = toggle;
		$(function() {
			$(self.toggle).click(function() {
				$(self.placeholder).unbind();
				if($(this).attr("value")=="bar") {
					self.drawPie();
					$(this).val("pie");
					$(this).html('<span class="ui-button-text">Show Bar Chart</span>'); //The class is for jquery ui
				} else {
					self.drawBar();
					$(this).val("bar");
					$(this).html('<span class="ui-button-text">Show Pie Chart</span>');
				};
			});
		});
	}
}