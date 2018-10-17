from tkinter import *
import math
import numpy
import PIL.Image
import os
import webbrowser
import html2text

class Drawer(Canvas):
    """a canvas where we can draw curves with the mouse"""
    def __init__(self, master, **arg):
        #init
        Canvas.__init__(self, master, **arg)
        #events
        self.bind('<Button-1>', self.draw)
        self.bind('<Motion>', self.pre_draw)
        self.bind('<Leave>', self.del_pre_draw)
        #vars
        self.ellipse=False
        self.pts=[]
        self.line=None
        self.drawing=IntVar(value=1)
        self.preline=None
        

    def del_pre_draw(self, event=None):
        try:
            self.delete(self.preline)
        except:
            print("can't delete:", self.preline)

    def pre_draw(self, event=None):
        self.del_pre_draw()
        if self.drawing.get():
            if self.ellipse:
                if len(self.pts)==1:
                    self.preline=self.create_oval(self.pts[0], (event.x, event.y))
                if len(self.pts)==2:
                    self.preline=self.create_oval(self.pts[1], (event.x, event.y))
            else:
                try:
                    self.preline=self.create_line(self.pts+[(event.x, event.y)], \
                                                      smooth=1, width=2)
                except:
                    pass

    def remove_last(self, event=None):
        self.pts=self.pts[:-1]
        self.draw()
    
    def restart_draw(self, event=None):
        self.del_draw()
        self.del_pre_draw()
        self.pts=[]
        self.line=None
        self.preline=None
    
    def del_draw(self, event=None):
        try:
            self.delete(self.line)
        except:
            print("can't delete:", self.line)
    
    def draw(self, event=None, width=5):
        self.del_draw()
        if not event is None:
            if self.drawing.get():
                self.pts.append((event.x, event.y))
            if self.ellipse:
                if len(self.pts)>2:
                    self.pts=self.pts[-2:]
        if len(self.pts)>1:
            if self.ellipse:
                self.line=self.create_oval(self.pts, width=width)
            else:
                self.line=self.create_line(self.pts, smooth=1, width=width)

def dist(pt1, pt2):
    return abs(pt1[0]-pt2[0])+abs(pt1[1]-pt2[1])

class Controler(Frame):
    """some controls and calculations to get the estimation of the lenght of \
curve that is draw in the canvas"""
    def __init__(self, master, canvas, **arg):
        #init
        Frame.__init__(self, master, **arg)
        self.can=canvas
        ##Frames
        f1=Frame(self, bd=2)
        f2=Frame(self, bd=2)
        f3=Frame(self, bd=2)
        f4=Frame(self, bd=2)
        f5=Frame(self, bd=2)

        ##### CONTROLS
        ###curve settings
        #bt erase
        self.bt_curve=Button(f1, text='Curve mode', command=self.curve_mode, \
                             fg='black', relief='sunken', bd=5, \
                             activeforeground='black', width=10)
        self.bt_curve.grid(row=1, column=0, pady=0, padx=0, sticky='w')
        #bt add
        self.bt_ellipse=Button(f1, text='Ellipse mode', \
                               command=self.ellipse_mode, \
                               fg='grey', relief='raised', bd=5, \
                               activeforeground='grey', width=10)
        self.bt_ellipse.grid(row=2, column=0, pady=0, padx=0, sticky='w')
        #others
        self.bt_stop=Button(f1, text='Restart the curve', \
                            command=canvas.restart_draw)
        self.bt_stop.grid(column=2, row=1, padx=20, pady=0)
        self.bt_rm=Button(f1, text='Remove last', \
                            command=canvas.remove_last)
        self.bt_rm.grid(column=2, row=2, padx=20, pady=0)
        self.ch_pre=Checkbutton(f1, text='Draw', \
                                variable=canvas.drawing)
        self.ch_pre.grid(column=1, rowspan=2, row=1, padx=10)
        
        ###lines settings
        #var
        self.linesSwitch=IntVar(value=0)
        self.lines=[]
        #scales
        self.sc_nb_lines=Scale(f2, orient='horizontal', from_=1, to=20,\
                               resolution=1, tickinterval=5, length=180,\
                               label='Number of lines:', relief='ridge', bd=5,\
                               command=self.draw_lines)
        self.sc_nb_lines.grid(column=1, columnspan=3, row=1, padx=5, pady=2)
        self.sc_angle=Scale(f2, orient='horizontal', from_=1, to=10,\
                               resolution=1, tickinterval=3, length=200,\
                               label='Angle between lines: pi/?', \
                               relief='ridge', bd=5, command=self.draw_lines)
        self.sc_angle.grid(column=4, columnspan=3, row=1, padx=5, pady=2)
        #bt
        self.ch_pre=Checkbutton(f2, text='Lines', command=self.draw_lines, \
                                variable=self.linesSwitch)
        self.ch_pre.grid(column=3, columnspan=2, row=2)

        ##dots settings
        #var
        self.dots_mode=False
        self.dots=[]
        self.dots_coord=[]
        #event
        canvas.bind('<Button-3>', self.dot)
        #bt erase all
        self.bt_erase_all=Button(f3, text='Erase all dots', bd=3, \
                                 command=self.del_dots, fg='grey')
        self.bt_erase_all.grid(row=2, column=1, pady=2, padx=5, sticky='e')
        #bt auto
        self.bt_auto=Button(f3, text='auto dots', fg='orange', bd=3, \
                            command=self.auto)
        self.bt_auto.grid(row=2, column=2, pady=2, padx=5, sticky='w')
        #bt erase
        self.bt_erase=Button(f3, text='Erase mode', command=self.erase_mode, \
                             fg='red', relief='sunken', bd=5, \
                             activeforeground='red')
        self.bt_erase.grid(row=3, column=1, pady=2, padx=5, sticky='e')
        #bt add
        self.bt_add=Button(f3, text='Add mode', command=self.add_mode, \
                           fg='green', relief='raised', bd=5, \
                           activeforeground='green')
        self.bt_add.grid(row=3, column=2, pady=2, padx=5, sticky='w')
        #lenght display
        self.lb_lenght=Label(f3, text='0 dot(s)\n\
lenght of the curve: 0.00 unit(s)', justify='left')
        self.lb_lenght.grid(row=1, column=1, columnspan=2, padx=10, pady=2)

        ###unit setting
        #var
        self.unit=None
        self.show_unit=IntVar(value=0)
        #scale
        self.sc_unit_lenght=Scale(f4, orient='horizontal', from_=-15, to=45,\
                                  resolution=0.5, tickinterval=0, length=200,\
                                  label='unit lenght', relief='ridge', bd=5,\
                                  command=self.draw_unit, showvalue=False)
        self.sc_unit_lenght.grid(row=1, column= 1, columnspan=4)
        #bt
        self.ch_unit=Checkbutton(f4, text='Show unit', \
                                 command=self.draw_unit, \
                                 variable=self.show_unit, fg='dark blue')
        self.ch_unit.grid(column=5, row=1)

        #? keep ?
        Button(f5, text='multi-auto', command=self.multi_auto).pack()
        
        ##packing frames
        f1.pack(pady=1, padx=5)
        f2.pack(pady=1, padx=5)
        f3.pack(pady=1, padx=5)
        f4.pack(pady=1, padx=5)
        f5.pack(pady=1, padx=5)

    def ellipse_mode(self, event=None):
        self.can.ellipse=True
        self.can.restart_draw()
        self.bt_ellipse.config(relief='sunken')
        self.bt_curve.config(relief='raised')

    def curve_mode(self, event=None):
        self.can.ellipse=False
        self.can.restart_draw()
        self.bt_curve.config(relief='sunken')
        self.bt_ellipse.config(relief='raised')
    
    def multi_auto(self, event=None):
        conv=[]
        for j in range(1,6):
            self.sc_angle.set(j)
            print('angle:', j)
            for i in range(1,10):
                self.sc_nb_lines.set(i)
                l=self.auto()
                conv.append(l)
                print(i, l)
        print()

    def dot(self, event=None):
        if self.dots_mode:
            self.add_dot(event)
        else:
            if len(self.dots_coord)>0:
                pt=(event.x, event.y)
                d=dist(pt, self.dots_coord[0])
                j=0
                new_dots=[]
                for i in range(1, len(self.dots_coord)):
                    if dist(pt, self.dots_coord[i])<d:
                        d=dist(pt, self.dots_coord[i])
                        new_dots.append(self.dots_coord[j])
                        j=int(i)
                    else:
                        new_dots.append(self.dots_coord[i])
                self.del_dots()
                for d in new_dots:
                    self.add_dot(pt=d)
    
    def erase_mode(self, event=None):
        self.dots_mode=False
        self.bt_erase.config(relief='sunken')
        self.bt_add.config(relief='raised')
    
    def add_mode(self, event=None):
        self.dots_mode=True
        self.bt_erase.config(relief='raised')
        self.bt_add.config(relief='sunken')

    def auto(self, event=None):
        #get values
        h=self.can.winfo_height()
        w=self.can.winfo_width()
        #drawer presets
        self.linesSwitch.set(1)
        self.show_unit.set(0)
        self.draw_unit()
        self.del_dots()
        #save curve
        self.can.draw(width=1)
        self.del_lines()
        file='temp.ps'
        self.can.postscript(file=file, colormode='mono')
        os.system('convert ' + file + ' ' + 'temp_curve.jpg')
        #lines
        self.can.del_draw()
        self.draw_lines()
        file='temp.ps'
        self.can.postscript(file=file, colormode='mono')
        os.system('convert ' + file + ' ' + 'temp_lines.jpg')
        #redraw lines & curve
        self.can.draw()
        #converting to matrices
        I = numpy.asarray(PIL.Image.open('temp_curve.jpg'))
        I2 = numpy.asarray(PIL.Image.open('temp_lines.jpg'))
        #logical 'and'
        M=numpy.logical_and(numpy.logical_not(I),numpy.logical_not(I2))
        xs, ys = numpy.nonzero(M)
        #rescaling
        pts=[]
        for i in range(len(xs)):
            pts.append((int(int(ys[i])*w/len(I[0])), int(int(xs[i])*h/len(I))))
        #delete repeated ones
        i=0
        while i<len(pts):
            j=i+1
            while j<len(pts):
                if dist(pts[i], pts[j])<7:
                    #print('del', pts[j])
                    del pts[j]
                else:
                    #print('+1', pts[j])
                    j+=1
            i+=1
        #display
        for pt in pts:
            self.add_dot(pt=pt)
        return self.calc_lenght()

    def draw_unit(self, event=None):
        self.can.delete(self.unit)
        if self.show_unit.get():
            h=self.can.winfo_height()
            w=self.can.winfo_width()
            l=0.01*(self.sc_unit_lenght.get()+20)
            self.unit=self.can.create_line(0.03*w, 0.95*h, (0.03+l)*w, 0.95*h,\
                                           width=10, fill='dark blue')
        self.calc_lenght()

    def draw_lines(self, event=None):
        self.del_lines()
        if self.linesSwitch.get():
            h=self.can.winfo_height()
            w=self.can.winfo_width()
            nb=self.sc_nb_lines.get()
            angle=self.sc_angle.get()
            rho=w/nb
            for n in range(1, angle):
                theta=n*(math.pi/(angle))
                for i in range(int(-0.5*nb), int(1.5*nb+1)):
                    pt1=(0, (i-0.5)*rho/(math.sin(theta)))
                    pt2=(w, ((i-0.5)*rho-500*math.cos(theta))/(math.sin(theta)))
                    l=self.can.create_line(pt1, pt2, width=0.5)
                    self.lines.append(l)
                    
            for i in range(nb):
                pt1=((i+0.5)*rho, 0)
                pt2=((i+0.5)*rho, h)
                l=self.can.create_line(pt1, pt2)
                self.lines.append(l)
        self.calc_lenght()
    
    def del_lines(self, event=None):
        for line in self.lines:
            try:
                self.can.delete(line)
            except:
                print("can't delete:", line)

    def del_dots(self, event=None):
        for dot in self.dots:
            try:
                self.can.delete(dot)
            except:
                print("can't delete:", dot)
        self.dots=[]
        self.dots_coord=[]
        self.calc_lenght()

    def add_dot(self, event=None, pt=None):
        rad=6
        if not event is None:
            pt=(event.x, event.y)
        dot=self.can.create_oval(pt[0]-rad, pt[1]-rad, \
                                 pt[0]+rad, pt[1]+rad,\
                                 fill='red')
        self.dots_coord.append(pt)
        self.dots.append(dot)
        self.calc_lenght()

    def calc_lenght(self, event=None):
        n=len(self.dots)
        w=self.can.winfo_width()
        #coef unit/dist_lines
        c=(w/self.sc_nb_lines.get()/(0.01*(self.sc_unit_lenght.get()+20)*w))
        #lenght estimation
        l=c*0.5*n*(math.pi/self.sc_angle.get())
        self.lb_lenght.config(text=str(n)+' dot(s)\n\
lenght of the curve: '+str(round(l, 3))+'0 unit(s)')
        return l
        
            
class HelpToolBar(Menu):
    def __init__(self, master):
        Menu.__init__(self, master)
        submenu=Menu(self)
        #self.add_command(label='Theory', command=self.theory)
        self.add_command(label='Documentation', command=self.doc)
        self.add_command(label='About us', command=self.about, \
                         activeforeground='white', \
                         activebackground='black')

    def doc(self, event=None):
        """display the documentation (how to use the program)"""
        #get text
        html_text = open('doc.html').read()
        txt=html2text.html2text(html_text)
        #new window
        top = Toplevel()
        top.title("DOC")
        Label(top, text=txt, justify='left').pack()
        top.mainloop()
    
    def about(self, event=None):
        """display information about us and the project"""
        #get text
        html_text = open('about.html').read()
        txt=html2text.html2text(html_text)
        #new window
        top = Toplevel()
        top.title("About us")
        Label(top, text=txt, justify='left').pack()
        top.mainloop()
    
    def theory(self, event=None):
        """open the pdf file that explains the theory"""
        #webbrowser.open('../Cauchy-Crofton_Formula.pdf')
        pass
    









if __name__=='__main__':
    #window
    root=Tk()
    root.title("Cauchy-Crafton's Formula")
    #icon
    imgicon = PhotoImage(file=os.path.join(os.getcwd(),'icon.png'))
    root.tk.call('wm', 'iconphoto', root._w, imgicon)
    
    #helpbar
    helpbar=HelpToolBar(root)
    root['menu']=helpbar
    #main drawer (canvas)
    can=Drawer(root, height=600, width=650, bg='white')
    can.grid(padx=5, pady=5, row=1, column=1)
    #main controler
    cont=Controler(root, can, bg='dark grey')
    cont.grid(row=1, column=2)

    #loop
    root.mainloop()

