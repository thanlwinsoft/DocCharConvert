/*------------------------------------------------------------------------
Copyright (C) 2002 SIL International. All rights reserved.

Distributable under the terms of either the Common Public License or the
GNU Lesser General Public License, as specified in the LICENSING.txt file.

File: Engine.h
Responsibility: Jonathan Kew
Last reviewed: Not yet.

Description:

-------------------------------------------------------------------------*/

#ifndef __Engine_H__
#define __Engine_H__

#include "TECkit_Engine.h"
#include "TECkit_Format.h"

const UInt32			kNeedMoreInput	= 0xfffffffeUL;
const UInt32			kInvalidChar	= 0xfffffffdUL;

class NeedMoreInputException	{};
class IncompleteCharException	{};

class Converter;

class Stage
	/* abstract base class for pipeline stages, both mapping and normalization passes */
{
public:
						Stage();
	virtual				~Stage();
	
	virtual UInt32		getChar() = 0;
	
	virtual void		Reset() = 0;

protected:
	friend class Converter;

	UInt32*				oBuffer;
	long				oBufSize;
	long				oBufEnd;	// points to next unused slot in oBuffer
	long				oBufPtr;	// points to next char to be returned to caller (if less than oBufEnd)

	Stage*				prevStage;
	Converter*			converter;
};

class Normalizer
	: public Stage
{
public:
						Normalizer(bool compose);
	virtual				~Normalizer();

	virtual UInt32		getChar();

	virtual void		Reset();

protected:	
	UInt32				process();
	
	void				decompose(UInt32 c);
	UInt32				decomposeOne(UInt32& c);
	
	void				compose();
	void				generateChar(UInt32 c);
	void				appendChar(UInt32 c);
	void				insertChar(UInt32 insCh, int insCombClass);
	void				growOutBuf();

	int					prevCombClass;
	long				oBufSafe;
	
	bool				bCompose;
};

class Pass
	: public Stage
{
public:
						Pass(const TableHeader* inTable);
	virtual				~Pass();
	
	virtual UInt32		getChar();

	virtual void		Reset();

protected:
	UInt32				DoMapping();

	void				outputChar(UInt32 c);

	UInt32				inputChar(long inIndex);
	void				advanceInput(unsigned int numChars);

	long				classMatch(UInt32 classNumber, UInt32 inChar) const;
	UInt32				repClassMember(UInt32 classNumber, UInt32 index) const;

	struct MatchInfo {
		UInt32			classIndex;
		struct {
			UInt16		start;
			UInt16		limit;
		}	matchedSpan;
	};

	UInt32				match(int index, int repeats, int textLoc);
								// returns 0 for no match, 1 for match, or kNeedMoreInput/kInvalidChar
	MatchElem*			pattern;
	int					patternLength;
	int					direction;
	MatchInfo			info[256];
	int					infoLimit;
	int					matchElems;
	int					matchedLength;

	int					groupRepeats;
	struct sgrStackItem {
		sgrStackItem*	link;
		int				savedGroupRepeats;
	};
	sgrStackItem*		sgrStack;

	const TableHeader*	tableHeader;

	const Byte*			pageBase;
	const Lookup*		lookupBase;
	const Byte*			matchClassBase;
	const Byte*			repClassBase;
	const Byte*			stringListBase;
	const Byte*			stringRuleData;
	const Byte*			planeMap;

	UInt32*				iBuffer;
	long				iBufSize;
	long				iBufStart;	// points to earliest valid char in iBuffer
	long				iBufEnd;	// points to next unused slot in iBuffer (one past last valid char)
	long				iBufPtr;	// points to next char to be fetched from iBuffer (if less than iBufEnd)

	bool				bInputIsUnicode;
	bool				bOutputIsUnicode;
	bool				bSupplementaryChars;
	UInt8				numPageMaps;
};

class Converter
	: public Stage
{
public:
						Converter(const Byte* inTable, UInt32 inTableSize, bool inForward,
									UInt16 inForm, UInt16 outForm);
						~Converter();

	TECkit_Status		ConvertBuffer(const Byte* inBuffer, UInt32 inLength, UInt32* inUsed,
							  Byte* outBuffer, UInt32 outLength, UInt32* outUsed,
							  bool inputIsComplete);

	virtual void		Reset();

	virtual UInt32		getChar();

	bool				IsForward() const;
	void				GetFlags(UInt32& sourceFlags, UInt32& targetFlags) const;
	bool				GetNamePtr(UInt16 inNameID, const Byte*& outNamePtr, UInt32& outNameLen) const;

	class Exception
	{
	public:
						Exception(UInt32 errCode)
							: errorCode(errCode)
							{ }
		UInt32			errorCode;
	};

	UInt32				creationStatus() const
							{ return status; }

	static bool			Validate(const Converter* cnv);

protected:
	friend class Pass;
	friend class Normalizer;

	UInt32				_getCharFn();
	UInt32				_getCharWithSavedBytes();
	void				_savePendingBytes();

	Byte*				table;
	
	Stage*				finalStage;
	
	const Byte*			data;
	UInt32				dataPtr;
	UInt32				dataLen;
	bool				inputComplete;

	bool				forward;

	Byte				inputForm;
	Byte				outputForm;

	Byte				savedBytes[8];
	UInt32				savedCount;

	UInt32				pendingOutputChar;
	long				status;
};

#endif /* __Engine_H__ */
