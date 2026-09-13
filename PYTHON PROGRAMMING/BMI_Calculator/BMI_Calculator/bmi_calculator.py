import tkinter as tk
from tkinter import messagebox

def calculate_bmi():
    try:
        weight = float(weight_entry.get())
        height = float(height_entry.get())

        if weight <= 0 or height <= 0:
            messagebox.showerror("Error", "Weight and height must be greater than 0.")
            return

        bmi = weight / (height ** 2)

        if bmi < 18.5:
            category = "Underweight"
        elif bmi < 25:
            category = "Normal"
        elif bmi < 30:
            category = "Overweight"
        else:
            category = "Obese"

        bmi_label.config(text=f"BMI: {bmi:.2f}")
        category_label.config(text=f"Category: {category}")

    except ValueError:
        messagebox.showerror(
            "Invalid Input",
            "Please enter numbers only.\nExample: Weight = 65, Height = 1.70"
        )

def clear_fields():
    weight_entry.delete(0, tk.END)
    height_entry.delete(0, tk.END)
    bmi_label.config(text="BMI: --")
    category_label.config(text="Category: --")

root = tk.Tk()
root.title("BMI Calculator")
root.geometry("400x350")
root.resizable(False, False)

title = tk.Label(
    root,
    text="BMI Calculator",
    font=("Arial", 24, "bold")
)
title.pack(pady=20)

tk.Label(root, text="Weight (kg)", font=("Arial", 12)).pack()
weight_entry = tk.Entry(root, font=("Arial", 12), width=25)
weight_entry.pack(pady=5)

tk.Label(root, text="Height (m)", font=("Arial", 12)).pack()
height_entry = tk.Entry(root, font=("Arial", 12), width=25)
height_entry.pack(pady=5)

button_frame = tk.Frame(root)
button_frame.pack(pady=15)

tk.Button(
    button_frame,
    text="Calculate",
    command=calculate_bmi,
    width=12
).grid(row=0, column=0, padx=5)

tk.Button(
    button_frame,
    text="Clear",
    command=clear_fields,
    width=12
).grid(row=0, column=1, padx=5)

bmi_label = tk.Label(
    root,
    text="BMI: --",
    font=("Arial", 18, "bold")
)
bmi_label.pack(pady=10)

category_label = tk.Label(
    root,
    text="Category: --",
    font=("Arial", 16, "bold")
)
category_label.pack(pady=5)

root.mainloop()
