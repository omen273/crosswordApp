#pragma once
#include <array>
#include <compare>
#include <queue>
#include <optional>
#include <stdexcept>
#include <string>
#include <tuple>
#include <unordered_set>
#include <vector>


namespace Utils
{
    struct Intersection final
    {
        Intersection(std::size_t x, std::size_t y) noexcept : firstWordPos(x), secondWordPos(y) {}
        std::size_t firstWordPos;
        std::size_t secondWordPos;

        //[[nodiscard]] auto operator<=> (const Intersection&) const = default;
    };

    //works only for Latin big letters
    [[nodiscard]] auto inline findIntersections(const std::string& str1, const std::string& str2)
    {
        std::vector<Intersection> res;
        std::array<std::vector<std::size_t>, 26> lettersStr;
        for (std::size_t i = 0; i < str1.size(); ++i) lettersStr[str1[i] - 'A'].push_back(i);
        for (std::size_t i = 0; i < str2.size(); ++i)
        {
            for (auto pos1 : lettersStr[str2[i] - 'A']) res.emplace_back(pos1, i);
        }

        return res;
    }

    struct wordsHashOrdered final
    {
        template<typename T>
        [[nodiscard]] auto operator()(const std::pair<T, T>& p) const noexcept
        {
            std::size_t h1 = std::hash<T>{}(p.first);
            std::size_t h2 = std::hash<T>{}(p.second);
            return h1 ^ (h2 << 1);
        }
    };


    [[nodiscard]] std::optional<std::vector<std::string>> inline findGroupWithSizeN(
            std::vector<std::string>::iterator begin,
        std::vector<std::string>::iterator end, std::size_t n)
    {
        using It = std::vector<std::string>::iterator;
        auto findConnectedGroup = [](It start, It n, It end)
        {
            auto intersected = start, last = std::next(start);
            while (last != n)
            {
                for (auto it = last; last != n && it != end; ++it)
                {
                    if (!findIntersections(*intersected, *it).empty())
                    {
                        std::iter_swap(last++, it);
                    }
                }
                if (++intersected == last) break;
            }
            return last;
        };

        auto start = begin;
        while(std::distance(start, end) >= n)
        {
            auto last = std::next(start, n);
            last = findConnectedGroup(start, last, end);
            if (std::distance(start, last) == n) return std::vector<std::string>{ start, last };
            start = last;
        }

        return std::nullopt;
    }

    struct Position final
    {
        int x;
        int y;

        auto& operator+=(const Position& pos) noexcept
        {
            x += pos.x, y += pos.y;
            return *this;
        }

        auto& operator-=(const Position& pos) noexcept
        {
            x -= pos.x, y -= pos.y;
            return *this;
        }

        [[nodiscard]] friend auto operator+(Position first, const Position& second) noexcept
        {
            return first += second;
        }

        [[nodiscard]] friend auto operator-(Position first, const Position& second) noexcept
        {
            return first -= second;
        }

        [[nodiscard]] friend auto operator==(const Position& first, const Position& second) noexcept
        {
            return std::tie(first.x, first.y) == std::tie(second.x, second.y);
        }

        [[nodiscard]] friend auto operator<(const Position& first, const Position& second) noexcept
        {
            return std::tie(first.x, first.y) < std::tie(second.x, second.y);
        }
    };

    struct positionHash final
    {
        [[nodiscard]] auto operator()(const Position& p) const noexcept
        {
            auto h1 = std::hash<int>{}(p.x);
            auto h2 = std::hash<int>{}(p.y);
            return h1 ^ (h2 << 1);
        }
    };

    struct Limits final
    {
        int top;
        int right;
        int bottom;
        int left;

        [[nodiscard]] friend auto operator==(const Limits& first, const Limits& second) noexcept
        {
            return std::tie(first.top, first.right, first.bottom, first.left) ==
                std::tie(second.top, second.right, second.bottom, second.left);
        }

        auto& operator+=(const Position& pos) noexcept
        {
            top += pos.y, right += pos.x, bottom += pos.y, left += pos.x;
            return *this;
        }

        [[nodiscard]] friend auto operator+(Limits l, const Position& pos) noexcept
        {
            return l += pos;
        }
    };

    enum class WordOrientation : bool
    {
        VERTICAL,
        HORIZONTAL
    };

    struct WordParams final
    {
        Utils::Position start;
        WordOrientation orientation;

        [[nodiscard]] friend auto operator==(const WordParams& first,
                const WordParams& second) noexcept
        {
            return std::tie(first.orientation, first.start) ==
            std::tie(second.orientation, second.start);
        }

        auto& operator+=(const Position& pos) noexcept
        {
            start += pos;
            return *this;
        }

        auto& operator-=(const Position& pos) noexcept
        {
            start -= pos;
            return *this;
        }

        [[nodiscard]] friend auto operator+(WordParams lhs, const Position& rhs) noexcept
        {
            return lhs += rhs;
        }

        [[nodiscard]] friend auto operator-(WordParams lhs, const Position& rhs) noexcept
        {
            return lhs -= rhs;
        }


    };

    struct insertionParams final
    {
        insertionParams(const WordParams& params, const Limits& limits, std::size_t n) noexcept :
            wordParams{ params }, limits{ limits }, crosswordIntersectionNumber{ n }{}
        WordParams wordParams;
        Limits limits;
        std::size_t crosswordIntersectionNumber;

        [[nodiscard]] friend auto operator==(const insertionParams& first,
                const insertionParams& second) noexcept
        {
            return std::tie(first.wordParams, first.limits, first.crosswordIntersectionNumber) ==
                std::tie(second.wordParams, second.limits, second.crosswordIntersectionNumber);
        }

        [[nodiscard]] friend auto operator+(insertionParams lhs, const Position& rhs) noexcept
        {
            lhs.wordParams += rhs, lhs.limits += rhs;
            return lhs;
        }
    };

    template<typename T>
    void toUpper(T begin, T end)
    {
        for (auto it = begin; it != end; ++it) for (auto& ch : *it) ch = std::toupper(ch);
    }
}
