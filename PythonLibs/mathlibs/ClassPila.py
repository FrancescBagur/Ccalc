#!/usr/bin/env python
#encoding: latin1

class Pila:
	"""Representa una pila buida amb operacions d'apilar, desapilar,
		i verificar si està buida. """

	def __init__(self):
		""" Crea una pila buida """
		#La pila buida es representa amb una llista buida
		self.items = []

	def apilar(self,x):
		""" Agrega un element a la pila. """
		#Apilar es agregar al final de la llista.
		self.items.append(x)

	def desapilar(self):
		""" Torna l'ultim element de la pila i l'elimina.
		    si la pila esta buida llença una excepció """
		try:
			return self.items.pop()
		except IndexError:
			raise ValueError("La pila està buida")

	def esta_buida(self):
		"""Torna true si la llista esta buida """
		return self.items == []

